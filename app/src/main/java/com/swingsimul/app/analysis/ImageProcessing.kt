package com.swingsimul.app.analysis

import android.graphics.Bitmap
import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** A single 8-bit luminance frame at analysis resolution. */
class GrayFrame(
    val width: Int,
    val height: Int,
    val pixels: ByteArray, // luminance as unsigned byte, row-major
) {
    fun lum(i: Int): Int = pixels[i].toInt() and 0xFF
}

/** A connected blob in a binary mask. */
class Blob(
    val area: Int,
    val cx: Float,
    val cy: Float,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
) {
    val bboxW: Int get() = maxX - minX + 1
    val bboxH: Int get() = maxY - minY + 1
    val fillRatio: Float get() = area.toFloat() / (bboxW * bboxH).coerceAtLeast(1)
    val aspect: Float get() = bboxW.toFloat() / bboxH.coerceAtLeast(1)
    val radius: Float get() = (bboxW + bboxH) / 4f
    val center: PointF get() = PointF(cx, cy)
}

object ImageProcessing {

    /** Scales [bitmap] to [targetWidth] and converts to luminance. */
    fun toGray(bitmap: Bitmap, targetWidth: Int): GrayFrame {
        val w = targetWidth.coerceAtMost(bitmap.width)
        val h = max(1, (bitmap.height.toLong() * w / bitmap.width).toInt())
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
        val argb = IntArray(w * h)
        scaled.getPixels(argb, 0, w, 0, 0, w, h)
        if (scaled != bitmap) scaled.recycle()
        val gray = ByteArray(w * h)
        for (i in argb.indices) {
            val c = argb[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            // Rec. 601 luma
            gray[i] = (((r * 77 + g * 150 + b * 29) shr 8) and 0xFF).toByte()
        }
        return GrayFrame(w, h, gray)
    }

    fun brightMask(frame: GrayFrame, threshold: Int): BooleanArray {
        val mask = BooleanArray(frame.pixels.size)
        for (i in frame.pixels.indices) mask[i] = frame.lum(i) >= threshold
        return mask
    }

    /** Absolute-difference mask between two same-size frames. */
    fun diffMask(a: GrayFrame, b: GrayFrame, threshold: Int): BooleanArray {
        val n = min(a.pixels.size, b.pixels.size)
        val mask = BooleanArray(n)
        for (i in 0 until n) mask[i] = abs(a.lum(i) - b.lum(i)) >= threshold
        return mask
    }

    /** 4-connected components via iterative flood fill. */
    fun connectedComponents(
        mask: BooleanArray,
        width: Int,
        height: Int,
        minArea: Int = 1,
    ): List<Blob> {
        val visited = BooleanArray(mask.size)
        val blobs = ArrayList<Blob>()
        val stack = IntArray(mask.size)
        for (start in mask.indices) {
            if (!mask[start] || visited[start]) continue
            var sp = 0
            stack[sp++] = start
            visited[start] = true
            var area = 0
            var sumX = 0L
            var sumY = 0L
            var minX = width
            var minY = height
            var maxX = 0
            var maxY = 0
            while (sp > 0) {
                val idx = stack[--sp]
                val x = idx % width
                val y = idx / width
                area++
                sumX += x
                sumY += y
                if (x < minX) minX = x
                if (y < minY) minY = y
                if (x > maxX) maxX = x
                if (y > maxY) maxY = y
                // neighbours
                if (x > 0) push(idx - 1, mask, visited, stack).let { if (it >= 0) stack[sp++] = it }
                if (x < width - 1) push(idx + 1, mask, visited, stack).let { if (it >= 0) stack[sp++] = it }
                if (y > 0) push(idx - width, mask, visited, stack).let { if (it >= 0) stack[sp++] = it }
                if (y < height - 1) push(idx + width, mask, visited, stack).let { if (it >= 0) stack[sp++] = it }
            }
            if (area >= minArea) {
                blobs.add(
                    Blob(
                        area = area,
                        cx = sumX.toFloat() / area,
                        cy = sumY.toFloat() / area,
                        minX = minX, minY = minY, maxX = maxX, maxY = maxY,
                    ),
                )
            }
        }
        return blobs
    }

    private fun push(idx: Int, mask: BooleanArray, visited: BooleanArray, stack: IntArray): Int {
        if (mask[idx] && !visited[idx]) {
            visited[idx] = true
            return idx
        }
        return -1
    }

    fun distance(a: PointF, b: PointF): Float = hypot(a.x - b.x, a.y - b.y)

    /**
     * Angle of a point set's principal axis above the horizontal, in degrees.
     * Oriented along the direction of travel (first → last point); positive means
     * the path rises. Returns null for degenerate input.
     */
    fun pathAngleDeg(points: List<PointF>): Double? {
        if (points.size < 3) return null
        var mx = 0f
        var my = 0f
        for (p in points) { mx += p.x; my += p.y }
        mx /= points.size
        my /= points.size
        var sxx = 0.0
        var syy = 0.0
        var sxy = 0.0
        for (p in points) {
            val dx = (p.x - mx).toDouble()
            val dy = (p.y - my).toDouble()
            sxx += dx * dx
            syy += dy * dy
            sxy += dx * dy
        }
        // Principal axis of the covariance matrix.
        val theta = 0.5 * atan2(2 * sxy, sxx - syy)
        var vx = kotlin.math.cos(theta)
        var vy = kotlin.math.sin(theta)
        // Orient along actual travel.
        val d = points.last()
        val f = points.first()
        if (vx * (d.x - f.x) + vy * (d.y - f.y) < 0) {
            vx = -vx
            vy = -vy
        }
        // Image y grows downward → negate for "up is positive".
        return Math.toDegrees(atan2(-vy, abs(vx)))
    }
}
