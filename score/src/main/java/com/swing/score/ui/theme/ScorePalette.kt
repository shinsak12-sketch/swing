package com.swing.score.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extra brand tokens that aren't part of the Material color scheme:
 * score-to-par coding and the convex card surfaces used throughout the app.
 */
data class ScorePalette(
    val birdie: Color,
    val bogey: Color,
    val doublePlus: Color,
    val parInk: Color,
    val positive: Color,
    val negative: Color,
    val cardTop: Color,
    val cardBottom: Color,
    val cardBorder: Color,
)

val LightScorePalette = ScorePalette(
    birdie = ScoreBirdie,
    bogey = ScoreBogey,
    doublePlus = ScoreDouble,
    parInk = Ink,
    positive = Fairway,
    negative = ScoreBirdie,
    cardTop = CardTopLight,
    cardBottom = CardBottomLight,
    cardBorder = CardBorderLight,
)

val DarkScorePalette = ScorePalette(
    birdie = ScoreBirdie,
    bogey = ScoreBogey,
    doublePlus = ScoreDouble,
    parInk = InkDark,
    positive = FairwayLight,
    negative = ScoreBirdie,
    cardTop = CardTopDark,
    cardBottom = CardBottomDark,
    cardBorder = CardBorderDark,
)

val LocalScorePalette = staticCompositionLocalOf { LightScorePalette }

/** Color for a hole score given strokes over/under par (0 = par). */
fun ScorePalette.forDiff(diff: Int): Color = when {
    diff <= -1 -> birdie
    diff == 0 -> parInk
    diff == 1 -> bogey
    else -> doublePlus
}
