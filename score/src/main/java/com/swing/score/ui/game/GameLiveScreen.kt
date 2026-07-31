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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.domain.BetEngine
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.components.RelStepper
import com.swing.score.ui.theme.FairwayDeep
import com.swing.score.ui.theme.LocalScorePalette

@Composable
fun GameLiveScreen(onSettle: () -> Unit, onBack: () -> Unit) {
    val players = GameSession.players
    val pars = GameSession.pars
    val tadang = GameSession.tadang

    if (players.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("진행 중인 내기가 없어요. 입력 탭에서 시작하세요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // Per-hole, per-player par-relative scores.
    val rels = remember {
        List(pars.size) { mutableStateListOf(*IntArray(players.size) { 0 }.toTypedArray()) }
    }
    var hole by remember { mutableIntStateOf(0) }

    // Running net (won/lost) across all holes entered so far.
    val strokesGrid = pars.indices.map { h -> players.indices.map { p -> pars[h] + rels[h][p] } }
    val net = BetEngine.stroke(players, strokesGrid, tadang).net

    val isLast = hole == pars.size - 1
    val palette = LocalScorePalette.current

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        // Hero — current hole
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, FairwayDeep)))
                .padding(16.dp),
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("진행 ${hole + 1} / ${pars.size}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    Text("스트로크 · 타당 %,d원".format(tadang), color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${hole + 1}", color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.ExtraBold)
                    Text("  HOLE · PAR ${pars[hole]}", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
                }
            }
        }

        Spacer12()

        // Player rows for this hole
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            players.forEachIndexed { p, name ->
                ConvexCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                money(net[p]),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (net[p] >= 0) palette.positive else palette.negative,
                            )
                        }
                        RelStepper(value = rels[hole][p], onChange = { rels[hole][p] = it }, min = -4, max = 8)
                    }
                }
            }
        }

        Spacer12()

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            if (hole > 0) {
                NavButton("← 이전 홀", Modifier.weight(1f), filled = false) { hole-- }
            }
            if (isLast) {
                NavButton("정산하기", Modifier.weight(1f), filled = true) {
                    GameSession.result = strokesGrid
                    onSettle()
                }
            } else {
                NavButton("다음 홀 →", Modifier.weight(1f), filled = true) { hole++ }
            }
        }

        Box(Modifier.height(16.dp))
    }
}

@Composable
private fun Spacer12() = Box(Modifier.height(12.dp))

@Composable
private fun NavButton(label: String, modifier: Modifier, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (filled) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

private fun money(v: Int): String = when {
    v > 0 -> "+%,d원".format(v)
    v < 0 -> "%,d원".format(v)
    else -> "0원"
}
