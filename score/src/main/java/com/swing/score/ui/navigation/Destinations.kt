package com.swing.score.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home("home", "홈", Icons.Outlined.Home),
    Rounds("rounds", "기록", Icons.Outlined.ListAlt),
    Entry("entry", "입력", Icons.Rounded.AddCircle),
    Stats("stats", "통계", Icons.Outlined.BarChart),
}
