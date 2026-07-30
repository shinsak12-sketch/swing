package com.swing.score.ui.stats

import androidx.compose.runtime.Composable
import com.swing.score.ui.components.ComingSoon

@Composable
fun StatsScreen() {
    ComingSoon(
        title = "통계",
        message = "라운드가 쌓이면 스코어 분포·파온율·약점 홀이\n자동으로 채워집니다.",
    )
}
