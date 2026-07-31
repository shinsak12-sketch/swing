package com.swing.score.ui.entry

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swing.score.ui.theme.FairwayDeep

@Composable
fun EntryScreen(
    onManual: () -> Unit,
    onCapturePicked: () -> Unit,
    onLiveGame: () -> Unit,
) {
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            CaptureDraft.uri = uri
            onCapturePicked()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.padding(top = 12.dp)) {
            Text(
                "라운드 기록하기",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "어떻게 입력할까요?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        BigChoice(
            icon = Icons.Outlined.PhotoCamera,
            title = "캡쳐 자동 인식",
            desc = "스코어카드 캡쳐를 고르면 홀별 스코어를 자동으로 읽어옵니다.",
            gradient = Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.primary, FairwayDeep)
            ),
            contentColor = Color.White,
            onClick = {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        )

        BigChoice(
            icon = Icons.Outlined.Edit,
            title = "직접 수기 입력",
            desc = "해외 라운드나 캡쳐가 없을 때. 홀별로 바로 입력합니다.",
            gradient = Brush.linearGradient(
                listOf(Color(0xFF1B2420), Color(0xFF10160F))
            ),
            contentColor = Color.White,
            onClick = onManual,
        )

        BigChoice(
            icon = Icons.Outlined.EmojiEvents,
            title = "라이브 내기",
            desc = "홀마다 입력하며 판돈이 실시간으로 정산됩니다. (스트로크·타당)",
            gradient = Brush.linearGradient(
                listOf(Color(0xFF2C8B5F), Color(0xFF0F4A30))
            ),
            contentColor = Color.White,
            onClick = onLiveGame,
        )

        Text(
            "이미지는 폰에서만 처리돼요. 서버로 올라가지 않습니다.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

@Composable
private fun BigChoice(
    icon: ImageVector,
    title: String,
    desc: String,
    gradient: Brush,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Column {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = title, tint = contentColor)
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                desc,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
