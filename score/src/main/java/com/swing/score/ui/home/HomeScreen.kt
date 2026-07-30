package com.swing.score.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.theme.FairwayDeep
import com.swing.score.ui.theme.LocalScorePalette

private data class RecentRound(
    val course: String,
    val detail: String,
    val score: Int,
    val toPar: Int,
)

private val sampleRounds = listOf(
    RecentRound("소노펠리체CC 비발디파크", "2026.07.25 · WEST", 95, 23),
    RecentRound("썬힐 골프클럽", "2026.04.25 · 힐→썬", 98, 26),
    RecentRound("Pebble Beach GL", "2026.03.11 · 수기입력", 91, 19),
)

private val sampleTrend = listOf(103, 99, 104, 96, 100, 94, 98, 91)

@Composable
fun HomeScreen() {
    val scroll = rememberScrollState()
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
                "이번 시즌 12 라운드",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Kpi(Modifier.weight(1f), "평균", "96")
            Kpi(Modifier.weight(1f), "베스트", "89")
            Kpi(Modifier.weight(1f), "핸디캡", "22.4")
        }

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                PanelHeader("스코어 추이", "최근 8라운드")
                Spacer(Modifier.height(10.dp))
                Sparkline(
                    values = sampleTrend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                )
            }
        }

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                PanelHeader("최근 라운드", "전체")
                Spacer(Modifier.height(4.dp))
                sampleRounds.forEachIndexed { index, round ->
                    if (index > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline),
                        )
                    }
                    RoundRow(round)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Kpi(modifier: Modifier, label: String, value: String) {
    ConvexCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
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
private fun PanelHeader(title: String, action: String) {
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
        )
    }
}

@Composable
private fun RoundRow(round: RecentRound) {
    val palette = LocalScorePalette.current
    Row(
        Modifier
            .fillMaxWidth()
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
                round.score.toString(),
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                round.course,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                round.detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            "+${round.toPar}",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = palette.bogey,
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
        drawPath(
            path = path,
            color = line,
            style = Stroke(width = 6f, cap = StrokeCap.Round),
        )
        drawCircle(color = endDot, radius = 8f, center = points.last())
    }
}
