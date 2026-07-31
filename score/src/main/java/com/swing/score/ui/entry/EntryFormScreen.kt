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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swing.score.domain.Round
import com.swing.score.domain.RoundRepository
import com.swing.score.domain.RoundSource
import com.swing.score.domain.StandardPars
import com.swing.score.ocr.ScorecardRecognizer
import com.swing.score.ui.components.ConvexCard
import com.swing.score.ui.components.RelStepper
import com.swing.score.ui.components.ScoreMark
import com.swing.score.ui.components.Stepper
import java.time.LocalDate

@Composable
fun EntryFormScreen(
    isCapture: Boolean,
    initialRound: Round?,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val editing = initialRound != null

    var courseName by remember { mutableStateOf(initialRound?.courseName ?: "") }
    val pars = remember { mutableStateListOf(*(initialRound?.pars ?: StandardPars).toTypedArray()) }
    // Par-relative scores: par = 0, birdie = -1, bogey = +1…
    val rels = remember {
        val initial = initialRound?.let { r -> r.strokes.mapIndexed { i, s -> s - r.pars[i] } }
            ?: List(pars.size) { 0 }
        mutableStateListOf(*initial.toTypedArray())
    }
    val today = remember { LocalDate.now().toString().replace('-', '.') }

    var recognizing by remember { mutableStateOf(isCapture && !editing) }
    var recogFailed by remember { mutableStateOf(false) }
    var checksumOk by remember { mutableStateOf<Boolean?>(null) }

    if (isCapture && !editing) {
        LaunchedEffect(Unit) {
            val uri = CaptureDraft.uri
            if (uri == null) {
                recognizing = false
                recogFailed = true
                return@LaunchedEffect
            }
            val result = ScorecardRecognizer.recognize(context, uri)
            if (result.success && result.pars.isNotEmpty()) {
                pars.clear(); pars.addAll(result.pars)
                rels.clear(); rels.addAll(result.strokes.mapIndexed { i, s -> s - result.pars[i] })
                checksumOk = result.checksumOk
            } else {
                recogFailed = true
            }
            recognizing = false
        }
    }

    val toPar = rels.sum()
    val total = pars.indices.sumOf { pars[it] + rels[it] }

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
                when {
                    editing -> "라운드 수정"
                    isCapture -> "인식 결과 확인"
                    else -> "수기 입력"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (isCapture && !editing) {
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
            RecognitionBanner(recognizing, recogFailed, checksumOk)
        }

        OutlinedTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = { Text("골프장 이름") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("합계", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "파 대비 기준으로 입력 (파 = E)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    "$total  (${if (toPar > 0) "+$toPar" else if (toPar == 0) "E" else "$toPar"})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        ConvexCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 4.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("홀", Modifier.width(40.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(Modifier.size(30.dp))
                    Text("PAR", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("파 대비", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                for (i in rels.indices) {
                    HoleRow(
                        hole = i + 1,
                        par = pars[i],
                        rel = rels[i],
                        onPar = { pars[i] = it },
                        onRel = { rels[i] = it },
                        showDivider = i > 0,
                    )
                }
            }
        }

        SaveButton(if (editing) "수정 저장" else "저장하기") {
            val strokes = pars.mapIndexed { i, p -> p + rels[i] }
            if (editing && initialRound != null) {
                RoundRepository.update(
                    initialRound.copy(
                        courseName = courseName.ifBlank { initialRound.courseName },
                        pars = pars.toList(),
                        strokes = strokes,
                    )
                )
                onSaved(initialRound.id)
            } else {
                val round = Round(
                    id = RoundRepository.newId(),
                    courseName = courseName.ifBlank { "내 라운드" },
                    subtitle = if (isCapture) "캡쳐 인식" else "수기 입력",
                    dateLabel = today,
                    pars = pars.toList(),
                    strokes = strokes,
                    source = if (isCapture) RoundSource.Capture else RoundSource.Manual,
                )
                RoundRepository.add(round)
                onSaved(round.id)
            }
        }

        Box(Modifier.height(16.dp))
    }
}

@Composable
private fun RecognitionBanner(recognizing: Boolean, failed: Boolean, checksumOk: Boolean?) {
    val (bg, fg, msg) = when {
        recognizing -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "캡쳐를 인식하는 중…",
        )
        failed -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "자동 인식에 실패했어요. 홀별 값을 직접 입력해 주세요.",
        )
        checksumOk == true -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "합계 검산 통과 · 값을 한 번 확인하세요.",
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "인식 완료 · 합계가 애매해요. 홀별로 확인해 주세요.",
        )
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (recognizing) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = fg)
        }
        Text(msg, color = fg, fontSize = 12.sp)
    }
}

@Composable
private fun HoleRow(
    hole: Int,
    par: Int,
    rel: Int,
    onPar: (Int) -> Unit,
    onRel: (Int) -> Unit,
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
            modifier = Modifier.width(40.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ScoreMark(stroke = par + rel, par = par, size = 26.dp)
        Box(Modifier.width(4.dp))
        Stepper(value = par, onChange = onPar, modifier = Modifier.weight(1f), min = 3, max = 6)
        RelStepper(value = rel, onChange = onRel, modifier = Modifier.weight(1f), min = -4, max = 8)
    }
}

@Composable
private fun SaveButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}
