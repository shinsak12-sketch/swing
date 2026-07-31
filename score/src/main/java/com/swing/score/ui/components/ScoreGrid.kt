package com.swing.score.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.ui.theme.LocalScorePalette

/**
 * A 9-hole segment (IN or OUT) with a par row and a colour-coded stroke row.
 * [startHole] is the label of the first hole in this segment (1-based).
 */
@Composable
fun ScoreSegment(
    pars: List<Int>,
    strokes: List<Int>,
    startHole: Int,
    modifier: Modifier = Modifier,
) {
    val subtotal = strokes.sum()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Cell("", header = true)
            for (i in pars.indices) Cell((startHole + i).toString(), header = true)
            Cell("합", header = true, emphasis = true)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Cell("PAR", rowLabel = true)
            for (p in pars) Cell(p.toString(), muted = true)
            Cell(pars.sum().toString(), muted = true, emphasis = true)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Cell("타수", rowLabel = true)
            for (i in strokes.indices) ScoreCell(strokes[i], strokes[i] - pars[i])
            Cell(subtotal.toString(), emphasis = true)
        }
    }
}

@Composable
private fun Cell(
    text: String,
    header: Boolean = false,
    rowLabel: Boolean = false,
    muted: Boolean = false,
    emphasis: Boolean = false,
) {
    val color = when {
        header || muted -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = if (rowLabel) Modifier.size(width = 30.dp, height = 26.dp)
        else Modifier.size(width = 28.dp, height = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = if (header) 10.sp else 11.sp,
            fontWeight = if (emphasis || rowLabel) FontWeight.ExtraBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = color,
        )
    }
}

@Composable
private fun ScoreCell(stroke: Int, diff: Int) {
    val palette = LocalScorePalette.current
    val (bg, fg, circle) = when {
        diff <= -1 -> Triple(palette.birdie, Color.White, true)
        diff == 0 -> Triple(Color.Transparent, MaterialTheme.colorScheme.onSurface, false)
        diff == 1 -> Triple(palette.bogey.copy(alpha = 0.16f), palette.bogey, false)
        else -> Triple(palette.doublePlus.copy(alpha = 0.16f), palette.doublePlus, false)
    }
    Box(
        modifier = Modifier.size(width = 28.dp, height = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(21.dp)
                .clip(if (circle) CircleShape else RoundedCornerShape(6.dp))
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stroke.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fg,
            )
        }
    }
}
