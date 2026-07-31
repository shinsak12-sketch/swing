package com.swing.score.ui.entry

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swing.score.domain.Round
import com.swing.score.domain.RoundRepository
import com.swing.score.domain.RoundSource
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.components.Stepper
import java.time.LocalDate

private val DEFAULT_PARS = listOf(4, 4, 3, 4, 5, 4, 4, 3, 5) + listOf(4, 4, 3, 4, 5, 4, 4, 3, 5)

@Composable
fun EntryFormScreen(
    isCapture: Boolean,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    var courseName by remember { mutableStateOf("") }
    val pars = remember { mutableStateListOf(*DEFAULT_PARS.toTypedArray()) }
    val strokes = remember { mutableStateListOf(*DEFAULT_PARS.toTypedArray()) }
    val today = remember { LocalDate.now().toString().replace('-', '.') }

    val total = strokes.sum()
    val toPar = total - pars.sum()

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
                if (isCapture) "인식 결과 확인" else "수기 입력",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (isCapture) {
            CaptureDraft.uri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "선택한 캡쳐",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
            ) {
                Text(
                    "이미지 자동 인식 엔진은 다음 단계에서 연결됩니다.\n지금은 홀별 값을 직접 확인·입력해 주세요.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 12.sp,
                )
            }
        }

        OutlinedTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = { Text("골프장 이름") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Live total
        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("합계", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "$total  (${if (toPar > 0) "+$toPar" else toPar})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // Hole rows
        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 4.dp)) {
                for (i in strokes.indices) {
                    HoleRow(
                        hole = i + 1,
                        par = pars[i],
                        stroke = strokes[i],
                        onPar = { pars[i] = it },
                        onStroke = { strokes[i] = it },
                        showDivider = i > 0,
                    )
                }
            }
        }

        SaveButton {
            val round = Round(
                id = RoundRepository.newId(),
                courseName = courseName.ifBlank { "내 라운드" },
                subtitle = if (isCapture) "캡쳐 인식" else "수기 입력",
                dateLabel = today,
                pars = pars.toList(),
                strokes = strokes.toList(),
                source = if (isCapture) RoundSource.Capture else RoundSource.Manual,
            )
            RoundRepository.add(round)
            onSaved(round.id)
        }

        Box(Modifier.height(16.dp))
    }
}

@Composable
private fun HoleRow(
    hole: Int,
    par: Int,
    stroke: Int,
    onPar: (Int) -> Unit,
    onStroke: (Int) -> Unit,
    showDivider: Boolean,
) {
    if (showDivider) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline),
        )
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${hole}번",
            modifier = Modifier.width(44.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(Modifier.weight(1f)) {
            Label("PAR")
            Stepper(value = par, onChange = onPar, min = 3, max = 6)
        }
        Column(Modifier.weight(1f)) {
            Label("타수")
            Stepper(value = stroke, onChange = onStroke, min = 1, max = 15)
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SaveButton(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("저장하기", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}
