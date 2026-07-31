package com.swing.score.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.domain.RoundRepository
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.components.ScoreSegment
import com.swing.score.ui.theme.FairwayDeep

@Composable
fun RoundDetailScreen(roundId: String, onBack: () -> Unit) {
    val round = RoundRepository.find(roundId)
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로")
            }
            Text(
                "라운드 상세",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (round == null) {
            Text(
                "라운드를 찾을 수 없어요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
            return@Column
        }

        // Hero
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, FairwayDeep))
                )
                .padding(16.dp),
        ) {
            Column {
                Text(round.courseName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "${round.dateLabel} · ${round.subtitle}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                )
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        round.total.toString(),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        "  ${plusSign(round.toPar)} · Par ${round.parTotal}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                val dist = round.distribution()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Quad(Modifier.weight(1f), dist[0], "버디↓")
                    Quad(Modifier.weight(1f), dist[1], "파")
                    Quad(Modifier.weight(1f), dist[2], "보기")
                    Quad(Modifier.weight(1f), dist[3], "더블+")
                }
            }
        }

        val half = round.holeCount / 2
        Segment("IN · ${round.strokes.take(half).sum()}", round.pars.take(half), round.strokes.take(half), 1)
        Segment("OUT · ${round.strokes.drop(half).sum()}", round.pars.drop(half), round.strokes.drop(half), 1)

        Box(Modifier.height(16.dp))
    }
}

@Composable
private fun Segment(title: String, pars: List<Int>, strokes: List<Int>, startHole: Int) {
    ConvexCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
            )
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                ScoreSegment(pars = pars, strokes = strokes, startHole = startHole)
            }
        }
    }
}

@Composable
private fun Quad(modifier: Modifier, value: Int, label: String) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp)
        }
    }
}

private fun plusSign(v: Int): String = if (v > 0) "+$v" else v.toString()
