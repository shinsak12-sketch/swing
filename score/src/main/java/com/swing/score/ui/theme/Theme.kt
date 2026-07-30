package com.swing.score.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Fairway,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = MintOnContainer,
    secondary = GreenHi,
    onSecondary = Color.White,
    background = GroundLight,
    onBackground = Ink,
    surface = SurfaceLight,
    onSurface = Ink,
    surfaceVariant = LineLight,
    onSurfaceVariant = InkSoft,
    outline = LineLight,
    outlineVariant = LineLight,
    error = ScoreBirdie,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = FairwayLight,
    onPrimary = Color(0xFF06311D),
    primaryContainer = FairwayDeep,
    onPrimaryContainer = MintContainer,
    secondary = GreenHi,
    onSecondary = Color(0xFF06311D),
    background = GroundDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = LineDark,
    onSurfaceVariant = InkSoftDark,
    outline = LineDark,
    outlineVariant = LineDark,
    error = ScoreBirdie,
    onError = Color.White,
)

@Composable
fun SwingScoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val scorePalette = if (darkTheme) DarkScorePalette else LightScorePalette

    CompositionLocalProvider(LocalScorePalette provides scorePalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
