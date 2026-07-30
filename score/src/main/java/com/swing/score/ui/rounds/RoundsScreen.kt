package com.swing.score.ui.rounds

import androidx.compose.runtime.Composable
import com.swing.score.ui.components.ComingSoon

@Composable
fun RoundsScreen() {
    ComingSoon(
        title = "기록",
        message = "저장된 라운드가 여기에 모입니다.\n입력 탭에서 첫 라운드를 추가해 보세요.",
    )
}
