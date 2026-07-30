package com.swingsimul.app.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swingsimul.app.ui.analysis.AnalysisScreen
import com.swingsimul.app.ui.importer.ImportScreen

object Routes {
    const val IMPORT = "import"
    const val ANALYSIS = "analysis"
    const val ARG_URI = "uri"
    fun analysis(uriString: String): String = "$ANALYSIS/${Uri.encode(uriString)}"
}

@Composable
fun SwingSimulNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.IMPORT) {
        composable(Routes.IMPORT) {
            ImportScreen(
                onPicked = { uriString ->
                    navController.navigate(Routes.analysis(uriString))
                },
            )
        }
        composable(
            route = "${Routes.ANALYSIS}/{${Routes.ARG_URI}}",
            arguments = listOf(navArgument(Routes.ARG_URI) { type = NavType.StringType }),
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString(Routes.ARG_URI).orEmpty()
            AnalysisScreen(
                uriString = uriString,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
