package com.swingsimul.app.data

import android.graphics.PointF

/**
 * Result of an automatic swing analysis. Angles are measured in the image plane
 * (scale- and frame-rate-independent), which is why they are estimable from a
 * single face-on super-slow-motion clip.
 */
data class SwingAnalysis(
    val impactFrame: Int,
    val ballCenter: PointF?,
    val ballRadius: Float,

    /** Club-head path angle at impact. Negative = descending into the ball. */
    val attackAngleDeg: Double?,
    val isDownBlow: Boolean?,

    /** Ball launch angle above horizontal, degrees. */
    val launchAngleDeg: Double?,

    /** Tracked centroids (in analysis-resolution pixels) for the overlay. */
    val clubPath: List<PointF>,
    val ballPath: List<PointF>,

    /** Resolution the detection ran at, so overlays can be scaled to full frame. */
    val analysisWidth: Int,
    val analysisHeight: Int,

    val notes: List<String>,
) {
    val hasAnyResult: Boolean
        get() = attackAngleDeg != null || launchAngleDeg != null
}
