package com.swingsimul.app.analysis

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.swingsimul.app.data.VideoTiming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads every video-sample presentation timestamp without decoding, so it stays
 * cheap even for long clips. The gaps between timestamps tell us the real
 * frame-rate structure of the imported super-slow-motion clip.
 */
object VideoTimingAnalyzer {

    suspend fun analyze(context: Context, uri: Uri): VideoTiming = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)

            val trackIndex = (0 until extractor.trackCount).firstOrNull { i ->
                extractor.getTrackFormat(i)
                    .getString(MediaFormat.KEY_MIME)
                    ?.startsWith("video/") == true
            } ?: error("영상 트랙을 찾을 수 없습니다.")

            val format = extractor.getTrackFormat(trackIndex)
            extractor.selectTrack(trackIndex)

            val pts = ArrayList<Long>(1024)
            while (true) {
                val sampleTime = extractor.sampleTime
                if (sampleTime < 0) break
                pts.add(sampleTime)
                if (!extractor.advance()) break
            }
            // Decode order can differ from display order; sort so gaps are real.
            pts.sort()

            val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) {
                format.getLong(MediaFormat.KEY_DURATION)
            } else {
                pts.lastOrNull() ?: 0L
            }

            VideoTiming(
                frameCount = pts.size,
                durationUs = durationUs,
                ptsUs = pts.toLongArray(),
            )
        } finally {
            extractor.release()
        }
    }
}
