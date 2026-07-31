package com.swing.score.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.swing.score.domain.RoundRepository
import com.swing.score.ui.detail.RoundDetailScreen
import com.swing.score.ui.entry.EntryFormScreen
import com.swing.score.ui.entry.EntryScreen
import com.swing.score.ui.home.HomeScreen
import com.swing.score.ui.navigation.TopDestination
import com.swing.score.ui.rounds.RoundsScreen
import com.swing.score.ui.stats.StatsScreen

@Composable
fun ScoreApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentHierarchy = backStackEntry?.destination?.hierarchy

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                TopDestination.entries.forEach { dest ->
                    val selected = currentHierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateToTab(dest.route) },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopDestination.Home.route) {
                HomeScreen(
                    onOpenRound = { id -> navController.navigate("detail/$id") },
                    onSeeAllRounds = { navController.navigateToTab(TopDestination.Rounds.route) },
                    onOpenStats = { navController.navigateToTab(TopDestination.Stats.route) },
                )
            }
            composable(TopDestination.Rounds.route) {
                RoundsScreen(onOpenRound = { id -> navController.navigate("detail/$id") })
            }
            composable(TopDestination.Entry.route) {
                EntryScreen(
                    onManual = { navController.navigate("form/manual") },
                    onCapturePicked = { navController.navigate("form/capture") },
                )
            }
            composable(TopDestination.Stats.route) { StatsScreen() }

            composable("detail/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                RoundDetailScreen(
                    roundId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { editId -> navController.navigate("edit/$editId") },
                )
            }
            composable("form/{mode}") { entry ->
                val isCapture = entry.arguments?.getString("mode") == "capture"
                EntryFormScreen(
                    isCapture = isCapture,
                    initialRound = null,
                    onSaved = { id ->
                        navController.navigate("detail/$id") {
                            popUpTo(TopDestination.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable("edit/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val round = RoundRepository.find(id)
                EntryFormScreen(
                    isCapture = false,
                    initialRound = round,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
