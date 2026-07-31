package com.swing.score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.ui.theme.LocalScorePalette

/** Signed stepper for par-relative entry: par shows as "E", else +N / -N, colour-coded. */
@Composable
fun RelStepper(
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = -5,
    max: Int = 10,
) {
    val palette = LocalScorePalette.current
    val label = when {
        value == 0 -> "E"
        value > 0 -> "+$value"
        else -> value.toString()
    }
    val color = when {
        value < 0 -> palette.under
        value == 0 -> MaterialTheme.colorScheme.onSurface
        else -> palette.over
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StepButton("−", enabled = value > min) { onChange((value - 1).coerceAtLeast(min)) }
        Text(
            label,
            modifier = Modifier.widthIn(min = 30.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
        )
        StepButton("+", enabled = value < max) { onChange((value + 1).coerceAtMost(max)) }
    }
}

@Composable
fun Stepper(
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 20,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StepButton("−", enabled = value > min) { onChange((value - 1).coerceAtLeast(min)) }
        Text(
            value.toString(),
            modifier = Modifier.widthIn(min = 22.dp),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        StepButton("+", enabled = value < max) { onChange((value + 1).coerceAtMost(max)) }
    }
}

@Composable
private fun StepButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val tint = if (enabled) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    Box(
        Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = tint)
    }
}
