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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swing.score.ui.components.ConvexCard

@Composable
fun GameSetupScreen(onStart: () -> Unit, onBack: () -> Unit) {
    val scroll = rememberScrollState()
    val names = remember { mutableStateListOf("나", "동반자 1", "", "") }
    var tadang by remember { mutableStateOf(1000) }
    val presets = listOf(500, 1000, 2000, 3000)

    val activePlayers = names.map { it.trim() }.filter { it.isNotEmpty() }
    val canStart = activePlayers.size >= 2

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
            Text("내기 설정", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        }

        Text("스트로크(타당)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "홀마다 타수 차이만큼 타당 금액으로 정산합니다.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("플레이어 (2~4명)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                for (i in names.indices) {
                    OutlinedTextField(
                        value = names[i],
                        onValueChange = { names[i] = it },
                        label = { Text("플레이어 ${i + 1}${if (i >= 2) " (선택)" else ""}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("타당 금액", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { amount ->
                        val selected = amount == tadang
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { tadang = amount }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "%,d".format(amount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
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
                .background(
                    if (canStart) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(enabled = canStart) {
                    GameSession.start(activePlayers, tadang)
                    onStart()
                }
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "라운드 시작",
                color = if (canStart) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Box(Modifier.height(16.dp))
    }
}
