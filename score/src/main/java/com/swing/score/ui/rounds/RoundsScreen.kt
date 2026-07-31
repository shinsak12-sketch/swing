package com.swing.score.ui.rounds

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import com.swing.score.domain.Round
import com.swing.score.domain.RoundRepository
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.theme.FairwayDeep
import com.swing.score.ui.theme.LocalScorePalette

@Composable
fun RoundsScreen(onOpenRound: (String) -> Unit) {
    val rounds by RoundRepository.rounds.collectAsState()
    var selectionMode by remember { mutableStateOf(false) }
    val selected = remember { mutableStateMapOf<String, Boolean>() }

    fun exitSelection() {
        selectionMode = false
        selected.clear()
    }

    val selectedCount = selected.count { it.value }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                IconButton(onClick = { exitSelection() }) {
                    Icon(Icons.Outlined.Close, contentDescription = "선택 취소")
                }
                Text(
                    "$selectedCount개 선택",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        RoundRepository.delete(selected.filterValues { it }.keys.toSet())
                        exitSelection()
                    },
                    enabled = selectedCount > 0,
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "삭제",
                        tint = if (selectedCount > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    "기록",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "길게 눌러 선택",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(rounds, key = { it.id }) { round ->
                RoundCard(
                    round = round,
                    selectionMode = selectionMode,
                    checked = selected[round.id] == true,
                    onClick = {
                        if (selectionMode) {
                            selected[round.id] = !(selected[round.id] ?: false)
                        } else {
                            onOpenRound(round.id)
                        }
                    },
                    onLongClick = {
                        selectionMode = true
                        selected[round.id] = true
                    },
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoundCard(
    round: Round,
    selectionMode: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val palette = LocalScorePalette.current
    ConvexCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (selectionMode) {
                Icon(
                    if (checked) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (checked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
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
