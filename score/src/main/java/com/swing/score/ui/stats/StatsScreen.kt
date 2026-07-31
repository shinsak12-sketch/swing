package com.swing.score.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.domain.Round
import com.swing.score.domain.RoundRepository
import com.swing.score.ui.components.ComingSoon
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.theme.LocalScorePalette
import kotlin.math.roundToInt

@Composable
fun StatsScreen() {
    val rounds by RoundRepository.rounds.collectAsState()
    if (rounds.isEmpty()) {
        ComingSoon(title = "통계", message = "라운드가 쌓이면 스코어 분포·파온율·약점 홀이\n자동으로 채워집니다.")
        return
    }

    val scroll = rememberScrollState()
    val holeCount = rounds.sumOf { it.holeCount }

    // distribution across all holes
    val dist = IntArray(4)
    for (r in rounds) for (i in 0 until r.holeCount) {
        when (r.diffAt(i)) {
            in Int.MIN_VALUE..-1 -> dist[0]++
            0 -> dist[1]++
            1 -> dist[2]++
            else -> dist[3]++
        }
    }

    val avg = rounds.map { it.total }.average().roundToInt()
    val best = rounds.minOf { it.total }
    val avgToPar = rounds.map { it.toPar }.average()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.padding(top = 12.dp)) {
            Text("통계", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "${rounds.size} 라운드 · $holeCount 홀",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Tile(Modifier.weight(1f), "평균", avg.toString())
            Tile(Modifier.weight(1f), "베스트", best.toString())
            Tile(Modifier.weight(1f), "평균 파대비", signed(avgToPar.roundToInt()))
        }

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("스코어 분포", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("홀 기준", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                val palette = LocalScorePalette.current
                DistRow("버디↓", dist[0], holeCount, palette.under)
                DistRow("파", dist[1], holeCount, MaterialTheme.colorScheme.onSurface)
                DistRow("보기", dist[2], holeCount, palette.over)
                DistRow("더블+", dist[3], holeCount, palette.onion)
            }
        }

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("파별 평균", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ParTile(Modifier.weight(1f), "Par 3", parAverage(rounds, 3))
                    ParTile(Modifier.weight(1f), "Par 4", parAverage(rounds, 4))
                    ParTile(Modifier.weight(1f), "Par 5", parAverage(rounds, 5))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun parAverage(rounds: List<Round>, par: Int): String {
    val diffs = ArrayList<Int>()
    for (r in rounds) for (i in 0 until r.holeCount) if (r.pars[i] == par) diffs.add(r.diffAt(i))
    if (diffs.isEmpty()) return "-"
    val avg = diffs.average()
    return if (avg > 0) "+%.1f".format(avg) else "%.1f".format(avg)
}

private fun signed(v: Int): String = if (v > 0) "+$v" else if (v == 0) "E" else "$v"

@Composable
private fun Tile(modifier: Modifier, label: String, value: String) {
    ConvexCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ParTile(modifier: Modifier, label: String, value: String) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DistRow(label: String, count: Int, total: Int, color: Color) {
    val pct = if (total > 0) count.toFloat() / total else 0f
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, Modifier.width(44.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Box(
            Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(pct)
                    .height(12.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color),
            )
        }
        Text("${(pct * 100).roundToInt()}%", Modifier.width(34.dp), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
