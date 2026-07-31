package com.swing.score.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.domain.BetEngine
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.theme.LocalScorePalette

@Composable
fun GameSettleScreen(onDone: () -> Unit) {
    val players = GameSession.players
    val palette = LocalScorePalette.current
    val scroll = rememberScrollState()

    if (players.isEmpty() || GameSession.result.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("정산할 내기가 없어요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val settlement = BetEngine.stroke(players, GameSession.result, GameSession.tadang)
    val totals = players.indices.map { p -> GameSession.result.sumOf { it[p] } }
    val ranking = players.indices.sortedByDescending { settlement.net[it] }

    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "최종 정산",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "18홀 종료 · 타당 %,d원".format(GameSession.tadang),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                ranking.forEachIndexed { rank, p ->
                    if (rank > 0) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${rank + 1}", Modifier.width(20.dp), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f)) {
                            Text(players[p], fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${totals[p]}타", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            money(settlement.net[p]),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (settlement.net[p] >= 0) palette.positive else palette.negative,
                        )
                    }
                }
            }
        }

        Text("누가 · 누구에게", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 2.dp))
        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                if (settlement.transfers.isEmpty()) {
                    Text("정산할 금액이 없어요 (무승부).", Modifier.padding(vertical = 12.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    settlement.transfers.forEachIndexed { idx, t ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(t.from, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("  →  ", fontSize = 13.sp, color = palette.negative)
                            Text(t.to, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "%,d원".format(t.amount),
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.positive,
                            )
                        }
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onDone)
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("완료", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }

        Box(Modifier.height(16.dp))
    }
}

private fun money(v: Int): String = when {
    v > 0 -> "+%,d원".format(v)
    v < 0 -> "%,d원".format(v)
    else -> "0원"
}
