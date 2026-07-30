package com.swingsimul.app.data

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * File-backed store for swing recordings. Videos live in the app's
 * external Movies directory (no storage permission required on API 19+).
 */
class RecordingRepository(context: Context) {

    private val appContext = context.applicationContext

    val moviesDir: File by lazy {
        val dir = File(
            appContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "swings",
        )
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    /** Creates a new, unique target file for an upcoming recording. */
    fun newRecordingFile(): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(System.currentTimeMillis()))
        return File(moviesDir, "swing_$stamp.mp4")
    }

    suspend fun list(): List<Recording> = withContext(Dispatchers.IO) {
        moviesDir.listFiles { f -> f.isFile && f.extension.equals("mp4", ignoreCase = true) }
            ?.map {
                Recording(
                    file = it,
                    name = it.nameWithoutExtension,
                    createdAt = it.lastModified(),
                    sizeBytes = it.length(),
                )
            }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    suspend fun delete(recording: Recording): Boolean = withContext(Dispatchers.IO) {
        recording.file.delete()
    }
}
