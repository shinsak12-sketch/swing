package com.swingsimul.app.data

/**
 * Per-frame timing extracted from a video track.
 *
 * The whole point of the first analysis step: a Samsung super-slow-motion clip
 * may either (a) preserve the real high-frame-rate timing in its presentation
 * timestamps, or (b) already be time-stretched so the container plays at 30fps.
 * The gaps between [ptsUs] reveal which case we are in on the actual device.
 */
class VideoTiming(
    val frameCount: Int,
    val durationUs: Long,
    val ptsUs: LongArray,
) {
    val avgFps: Double
        get() = if (durationUs > 0) frameCount / (durationUs / 1_000_000.0) else 0.0

    /** Gaps between consecutive presentation timestamps, in microseconds. */
    val deltasUs: LongArray by lazy {
        if (ptsUs.size < 2) LongArray(0)
        else LongArray(ptsUs.size - 1) { i -> ptsUs[i + 1] - ptsUs[i] }
    }

    val medianDeltaUs: Long by lazy {
        if (deltasUs.isEmpty()) 0L else deltasUs.sorted()[deltasUs.size / 2]
    }

    val minDeltaUs: Long get() = deltasUs.minOrNull() ?: 0L
    val maxDeltaUs: Long get() = deltasUs.maxOrNull() ?: 0L

    /**
     * Longest contiguous run of frames whose gaps are markedly shorter than the
     * median — i.e. a genuine high-frame-rate segment preserved in the file.
     * Null when the timing is uniform (already stretched by the container), which
     * itself is a useful finding.
     */
    val slowSegment: SlowSegment? by lazy { detectSlowSegment() }

    private fun detectSlowSegment(): SlowSegment? {
        if (deltasUs.size < 3) return null
        val median = medianDeltaUs
        if (median <= 0) return null
        val threshold = median / 2

        var bestStart = -1
        var bestLen = 0
        var curStart = -1
        var curLen = 0
        for (i in deltasUs.indices) {
            if (deltasUs[i] < threshold) {
                if (curStart < 0) {
                    curStart = i
                    curLen = 0
                }
                curLen++
            } else {
                if (curLen > bestLen) {
                    bestLen = curLen
                    bestStart = curStart
                }
                curStart = -1
                curLen = 0
            }
        }
        if (curLen > bestLen) {
            bestLen = curLen
            bestStart = curStart
        }
        if (bestStart < 0 || bestLen < 2) return null

        var sum = 0L
        for (i in bestStart until bestStart + bestLen) sum += deltasUs[i]
        val avg = sum / bestLen
        val fps = if (avg > 0) 1_000_000.0 / avg else 0.0
        // delta index i sits between frame i and frame i+1
        return SlowSegment(
            startFrame = bestStart,
            endFrame = bestStart + bestLen,
            impliedFps = fps,
        )
    }
}

data class SlowSegment(
    val startFrame: Int,
    val endFrame: Int,
    val impliedFps: Double,
)
