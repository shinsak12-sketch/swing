package com.swing.score.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.domain.Round
import com.swing.score.domain.RoundRepository
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.theme.FairwayDeep
import com.swing.score.ui.theme.LocalScorePalette

@Composable
fun HomeScreen(
    onOpenRound: (String) -> Unit,
    onSeeAllRounds: () -> Unit,
    onOpenStats: () -> Unit,
) {
    val scroll = rememberScrollState()
    val rounds by RoundRepository.rounds.collectAsState()

    val average = rounds.map { it.total }.takeIf { it.isNotEmpty() }?.average()?.let { Math.round(it).toInt() }
    val best = rounds.minOfOrNull { it.total }
    val trend = rounds.map { it.total }.asReversed()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Column {
            Text(
                "안녕하세요, 신이삭님",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "이번 시즌 ${rounds.size} 라운드",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Kpi(Modifier.weight(1f), "평균", average?.toString() ?: "-", onOpenStats)
            Kpi(Modifier.weight(1f), "베스트", best?.toString() ?: "-", onOpenStats)
            Kpi(Modifier.weight(1f), "핸디캡", "22.4", onOpenStats)
        }

        if (trend.size >= 2) {
            ConvexCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    PanelHeader("스코어 추이", "최근 ${trend.size}라운드", onOpenStats)
                    Spacer(Modifier.height(10.dp))
                    Sparkline(
                        values = trend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                    )
                }
            }
        }

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                PanelHeader("최근 라운드", "전체", onSeeAllRounds)
                Spacer(Modifier.height(4.dp))
                rounds.take(3).forEachIndexed { index, round ->
                    if (index > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline),
                        )
                    }
                    RoundRow(round) { onOpenRound(round.id) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Kpi(modifier: Modifier, label: String, value: String, onClick: () -> Unit) {
    ConvexCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(
            Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PanelHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            action,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onAction),
        )
    }
}

@Composable
private fun RoundRow(round: Round, onClick: () -> Unit) {
    val palette = LocalScorePalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, FairwayDeep)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                round.total.toString(),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                round.courseName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                "${round.dateLabel} · ${round.subtitle}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            if (round.toPar > 0) "+${round.toPar}" else round.toPar.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (round.toPar > 0) palette.bogey else palette.positive,
        )
    }
}

@Composable
private fun Sparkline(values: List<Int>, modifier: Modifier) {
    val line = MaterialTheme.colorScheme.primary
    val endDot = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val span = (max - min).coerceAtLeast(1)
        val stepX = size.width / (values.size - 1)
        val pad = 6f
        val usableH = size.height - pad * 2
        val points = values.mapIndexed { i, v ->
            val x = stepX * i
            val y = pad + usableH * (1f - (v - min).toFloat() / span)
            Offset(x, y)
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (p in points.drop(1)) lineTo(p.x, p.y)
        }
        drawPath(path = path, color = line, style = Stroke(width = 6f, cap = StrokeCap.Round))
        drawCircle(color = endDot, radius = 8f, center = points.last())
    }
}
