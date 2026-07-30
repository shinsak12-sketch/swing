package com.swingsimul.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = LimeAccent,
    onPrimary = Ink,
    secondary = TeeYellow,
    onSecondary = Ink,
    background = Ink,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    error = RecordRed,
)

private val LightColors = lightColorScheme(
    primary = FairwayGreen,
    onPrimary = Color.White,
    secondary = TeeYellow,
    onSecondary = Ink,
    background = Color.White,
    onBackground = Ink,
    surface = SandBeige,
    onSurface = Ink,
    error = RecordRed,
)

@Composable
fun SwingSimulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
