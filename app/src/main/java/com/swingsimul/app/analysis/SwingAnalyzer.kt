package com.swingsimul.app.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.swingsimul.app.data.SwingAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Automatic, single-camera (face-on) swing analysis.
 *
 * Pipeline, all in the image plane so results are scale- and frame-rate free:
 *  1. Decode every frame to a small grayscale image.
 *  2. Find the ball at rest (a stationary bright round blob).
 *  3. Find the impact frame (the ball leaves its rest position).
 *  4. Track the club head into impact  → attack angle / down-blow.
 *  5. Track the ball just after impact → launch angle.
 *
 * The heuristics are deliberate and tunable; the overlay lets us see what was
 * detected so thresholds can be calibrated against real footage.
 */
class SwingAnalyzer(private val context: Context) {

    private val analysisWidth = 480

    suspend fun analyze(
        uri: Uri,
        onProgress: (Float) -> Unit = {},
    ): SwingAnalysis = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val frameCount = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                ?.toIntOrNull() ?: 0
            require(frameCount >= 4) { "분석하기엔 프레임이 너무 적습니다 ($frameCount)." }

            // 1. Decode all frames to grayscale (small).
            val frames = ArrayList<GrayFrame>(frameCount)
            for (i in 0 until frameCount) {
                val bmp = try {
                    retriever.getFrameAtIndex(i)
                } catch (e: Exception) {
                    null
                } ?: continue
                frames.add(ImageProcessing.toGray(bmp, analysisWidth))
                bmp.recycle()
                if (i % 5 == 0) onProgress(i.toFloat() / frameCount)
            }
            onProgress(0.9f)

            require(frames.size >= 4) { "프레임 디코딩에 실패했습니다." }
            val w = frames[0].width
            val h = frames[0].height
            val notes = ArrayList<String>()

            // 2. Ball at rest.
            val ball = detectRestingBall(frames, w, h, notes)

            // 3. Impact frame + ball tracking.
            var impactFrame = frames.size / 2
            var ballPath: List<PointF> = emptyList()
            var launchAngle: Double? = null
            if (ball != null) {
                val tracked = trackBallDeparture(frames, ball, w, h)
                if (tracked != null) {
                    impactFrame = tracked.impactIndex
                    ballPath = tracked.path
                    launchAngle = ImageProcessing.pathAngleDeg(tracked.path)
                    if (launchAngle == null) notes.add("볼 비행 궤적이 짧아 발사각을 못 구했어요.")
                } else {
                    notes.add("임팩트(공이 떠나는 순간)를 못 찾았어요.")
                }
            } else {
                notes.add("정지된 공을 못 찾았어요. 카메라를 정면·고정으로, 공이 밝게 보이게 찍어보세요.")
            }

            // 4. Club head approach → attack angle.
            val clubPath = if (ball != null) {
                trackClubApproach(frames, impactFrame, ball, w, h)
            } else {
                emptyList()
            }
            val attackAngle = ImageProcessing.pathAngleDeg(clubPath)
            if (attackAngle == null && ball != null) {
                notes.add("헤드 접근 궤적을 충분히 못 잡았어요.")
            }
            val downBlow = attackAngle?.let { it < 0 }

            onProgress(1f)
            SwingAnalysis(
                impactFrame = impactFrame,
                ballCenter = ball?.center,
                ballRadius = ball?.radius ?: 0f,
                attackAngleDeg = attackAngle,
                isDownBlow = downBlow,
                launchAngleDeg = launchAngle,
                clubPath = clubPath,
                ballPath = ballPath,
                analysisWidth = w,
                analysisHeight = h,
                notes = notes,
            )
        } finally {
            retriever.release()
        }
    }

    /** Re-decodes one frame at full resolution (for drawing the overlay). */
    fun decodeFrame(uri: Uri, index: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.getFrameAtIndex(index)
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    // ---- Stage helpers -----------------------------------------------------

    private fun detectRestingBall(
        frames: List<GrayFrame>,
        w: Int,
        h: Int,
        notes: MutableList<String>,
    ): Blob? {
        val minR = w * 0.006f          // ~3px at 480w
        val maxR = w * 0.08f           // generous upper bound
        val minArea = (Math.PI * minR * minR).toInt().coerceAtLeast(4)
        val maxArea = (Math.PI * maxR * maxR).toInt()

        fun bestBallIn(frame: GrayFrame): Blob? {
            // Try a few brightness thresholds; the ball is among the brightest.
            for (threshold in intArrayOf(220, 200, 180)) {
                val mask = ImageProcessing.brightMask(frame, threshold)
                val blobs = ImageProcessing.connectedComponents(mask, w, h, minArea)
                val candidate = blobs
                    .filter { it.area <= maxArea && it.fillRatio > 0.5f && it.aspect in 0.55f..1.8f }
                    // Prefer round, sizeable blobs in the lower-central area.
                    .maxByOrNull { it.area.toFloat() * centralityWeight(it, w, h) }
                if (candidate != null) return candidate
            }
            return null
        }

        val sampleCount = frames.size.coerceAtMost(8)
        val candidates = (0 until sampleCount).mapNotNull { bestBallIn(frames[it]) }
        if (candidates.isEmpty()) return null

        // Require a stationary cluster: median center with small spread.
        val cx = candidates.map { it.cx }.sorted()[candidates.size / 2]
        val cy = candidates.map { it.cy }.sorted()[candidates.size / 2]
        val median = PointF(cx, cy)
        val stable = candidates.filter { ImageProcessing.distance(it.center, median) < maxR }
        if (stable.size < 2) {
            notes.add("공이 고정돼 보이지 않아요(주소 자세가 잠깐이라도 잡혀야 정확합니다).")
        }
        return stable.maxByOrNull { it.area } ?: candidates.first()
    }

    private fun centralityWeight(blob: Blob, w: Int, h: Int): Float {
        // Slight preference for lower-center, where the ball usually sits.
        val nx = 1f - abs(blob.cx / w - 0.5f)
        val ny = blob.cy / h // lower is bigger
        return 0.5f + 0.25f * nx + 0.25f * ny
    }

    private class BallTrack(val impactIndex: Int, val path: List<PointF>)

    private fun trackBallDeparture(
        frames: List<GrayFrame>,
        ball: Blob,
        w: Int,
        h: Int,
    ): BallTrack? {
        val rest = ball.center
        val r = ball.radius.coerceAtLeast(3f)
        val leaveDist = r * 1.5f
        val searchR = r * 6f
        val minArea = (Math.PI * (r * 0.4f) * (r * 0.4f)).toInt().coerceAtLeast(3)

        var impact = -1
        val path = ArrayList<PointF>()
        var last = rest
        for (i in frames.indices) {
            val frame = frames[i]
            val moving = findBrightBlobNear(frame, last, searchR, minArea, w, h)
            if (impact < 0) {
                // Still waiting for the ball to leave the tee.
                if (moving != null && ImageProcessing.distance(moving.center, rest) > leaveDist) {
                    impact = i
                    path.add(moving.center)
                    last = moving.center
                }
            } else {
                if (moving == null) break
                // Ball should keep moving away; stop if it stalls (lost track).
                if (ImageProcessing.distance(moving.center, last) < r * 0.3f) break
                path.add(moving.center)
                last = moving.center
                if (path.size >= 8) break
                if (moving.center.x < r || moving.center.x > w - r ||
                    moving.center.y < r || moving.center.y > h - r
                ) break
            }
        }
        if (impact < 0 || path.size < 2) return null
        return BallTrack(impactIndex = (impact - 1).coerceAtLeast(0), path = path)
    }

    private fun findBrightBlobNear(
        frame: GrayFrame,
        near: PointF,
        radius: Float,
        minArea: Int,
        w: Int,
        h: Int,
    ): Blob? {
        for (threshold in intArrayOf(210, 190, 170)) {
            val mask = ImageProcessing.brightMask(frame, threshold)
            val blobs = ImageProcessing.connectedComponents(mask, w, h, minArea)
            val hit = blobs
                .filter { ImageProcessing.distance(it.center, near) <= radius }
                .maxByOrNull { it.area }
            if (hit != null) return hit
        }
        return null
    }

    private fun trackClubApproach(
        frames: List<GrayFrame>,
        impactFrame: Int,
        ball: Blob,
        w: Int,
        h: Int,
    ): List<PointF> {
        val start = (impactFrame - 6).coerceAtLeast(1)
        val end = impactFrame.coerceAtMost(frames.size - 1)
        if (end - start < 2) return emptyList()

        val nearR = ball.radius.coerceAtLeast(3f) * 12f
        val minArea = (ball.radius * ball.radius * 0.5f).toInt().coerceAtLeast(6)
        val points = ArrayList<PointF>()
        for (i in (start + 1)..end) {
            val mask = ImageProcessing.diffMask(frames[i - 1], frames[i], 28)
            val blobs = ImageProcessing.connectedComponents(mask, w, h, minArea)
            // The club head is the largest motion blob within reach of the ball.
            val head = blobs
                .filter { ImageProcessing.distance(it.center, ball.center) <= nearR }
                .maxByOrNull { it.area }
            if (head != null) points.add(head.center)
        }
        return points
    }

    companion object {
        fun formatAngle(deg: Double?): String =
            if (deg == null) "—" else "%+.1f°".format(deg)

        fun formatIndex(i: Int): String = "#$i"

        @Suppress("unused")
        fun roundDeg(deg: Double): Int = deg.roundToInt()
    }
}
