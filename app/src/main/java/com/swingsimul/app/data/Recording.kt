package com.swingsimul.app.data

import android.net.Uri
import java.io.File

/** A single captured swing video stored in app-specific external storage. */
data class Recording(
    val file: File,
    val name: String,
    val createdAt: Long,
    val sizeBytes: Long,
) {
    val uri: Uri get() = Uri.fromFile(file)
}
