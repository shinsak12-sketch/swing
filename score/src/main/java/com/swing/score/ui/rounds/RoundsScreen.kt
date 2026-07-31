package com.swing.score.ui.rounds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.domain.Round
import com.swing.score.domain.RoundRepository
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.theme.FairwayDeep
import com.swing.score.ui.theme.LocalScorePalette

@Composable
fun RoundsScreen(onOpenRound: (String) -> Unit) {
    val rounds by RoundRepository.rounds.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "기록",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
            )
        }
        items(rounds, key = { it.id }) { round ->
            RoundCard(round) { onOpenRound(round.id) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun RoundCard(round: Round, onClick: () -> Unit) {
    val palette = LocalScorePalette.current
    ConvexCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, FairwayDeep))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    round.total.toString(),
                    color = Color.White,
                    fontSize = 18.sp,
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
}
