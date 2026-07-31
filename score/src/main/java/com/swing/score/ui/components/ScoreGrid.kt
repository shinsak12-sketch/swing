package com.swing.score.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.ui.theme.LocalScorePalette
import kotlin.math.abs
import kotlin.math.min

/**
 * A 9-hole segment (IN or OUT) with a par row and a PGA-style stroke row.
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
            for (i in strokes.indices) {
                Box(Modifier.size(width = 28.dp, height = 26.dp), contentAlignment = Alignment.Center) {
                    ScoreMark(stroke = strokes[i], par = pars[i])
                }
            }
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

/**
 * PGA-style score mark:
 * - under par → red concentric circles (birdie 1, eagle 2, albatross 3…)
 * - par → plain number
 * - over par → blue concentric squares (bogey 1, double 2, triple 3, quad 4…)
 * - double par or worse ("양파") → filled marker
 */
@Composable
fun ScoreMark(stroke: Int, par: Int, size: androidx.compose.ui.unit.Dp = 24.dp) {
    val palette = LocalScorePalette.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    val diff = stroke - par
    val isOnion = par > 0 && stroke >= par * 2

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        when {
            isOnion -> {
                Box(
                    Modifier
                        .size(size * 0.86f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.onion),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stroke.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            diff == 0 -> {
                Text(stroke.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onSurface)
            }
            else -> {
                val rings = min(abs(diff), 4)
                val circle = diff < 0
                val color = if (circle) palette.under else palette.over
                Canvas(Modifier.size(size)) {
                    val strokePx = 1.5.dp.toPx()
                    val gap = 3.dp.toPx()
                    for (i in 0 until rings) {
                        val inset = strokePx / 2 + i * gap
                        if (circle) {
                            drawCircle(
                                color = color,
                                radius = this.size.minDimension / 2 - inset,
                                style = Stroke(width = strokePx),
                            )
                        } else {
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(inset, inset),
                                size = Size(this.size.width - inset * 2, this.size.height - inset * 2),
                                cornerRadius = CornerRadius(3.dp.toPx()),
                                style = Stroke(width = strokePx),
                            )
                        }
                    }
                }
                Text(stroke.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurface)
            }
        }
    }
}
