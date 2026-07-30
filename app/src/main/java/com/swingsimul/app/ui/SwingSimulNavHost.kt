package com.swingsimul.app.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swingsimul.app.ui.camera.CameraScreen
import com.swingsimul.app.ui.gallery.GalleryScreen
import com.swingsimul.app.ui.playback.PlaybackScreen
import java.io.File

object Routes {
    const val CAMERA = "camera"
    const val GALLERY = "gallery"
    const val PLAYBACK = "playback"
    const val ARG_PATH = "path"
    fun playback(filePath: String): String =
        "$PLAYBACK/${Uri.encode(filePath)}"
}

@Composable
fun SwingSimulNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.CAMERA) {
        composable(Routes.CAMERA) {
            CameraScreen(
                onOpenGallery = { navController.navigate(Routes.GALLERY) },
                onRecorded = { file ->
                    navController.navigate(Routes.playback(file.absolutePath))
                },
            )
        }
        composable(Routes.GALLERY) {
            GalleryScreen(
                onBack = { navController.popBackStack() },
                onOpenRecording = { recording ->
                    navController.navigate(Routes.playback(recording.file.absolutePath))
                },
            )
        }
        composable(
            route = "${Routes.PLAYBACK}/{${Routes.ARG_PATH}}",
            arguments = listOf(navArgument(Routes.ARG_PATH) { type = NavType.StringType }),
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString(Routes.ARG_PATH).orEmpty()
            PlaybackScreen(
                file = File(path),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
