package com.swing.score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.swing.score.ui.theme.LocalScorePalette

/**
 * A softly raised ("convex") card: vertical highlight-to-shade gradient,
 * a hairline border and a grounded drop shadow. The core surface treatment
 * for the whole app.
 */
@Composable
fun ConvexCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    content: @Composable () -> Unit,
) {
    val palette = LocalScorePalette.current
    Box(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = shape, clip = false)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(palette.cardTop, palette.cardBottom))
            )
            .border(1.dp, palette.cardBorder, shape),
    ) {
        content()
    }
}
