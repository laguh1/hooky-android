package com.hooky.app.ui.util

import android.content.Context
import java.io.File

fun movePhotosToStorage(
    context: Context,
    photos: List<String>,
    entityType: String,
    entityId: String
): List<String> {
    val destDir = File(context.filesDir, "photos/$entityType/$entityId").also { it.mkdirs() }
    return photos.map { path ->
        val src = File(path)
        if (!src.exists()) return@map path
        if (src.absolutePath.startsWith(destDir.absolutePath)) return@map path
        val dest = File(destDir, src.name)
        if (src.renameTo(dest)) dest.absolutePath else {
            // renameTo can fail across filesystems — fallback to copy+delete
            try {
                src.copyTo(dest, overwrite = true)
                src.delete()
                dest.absolutePath
            } catch (_: Exception) {
                path
            }
        }
    }
}

fun moveSingleFileToStorage(
    context: Context,
    path: String,
    entityType: String,
    entityId: String
): String {
    val src = File(path)
    if (!src.exists()) return path
    val destDir = File(context.filesDir, "$entityType/$entityId").also { it.mkdirs() }
    if (src.absolutePath.startsWith(destDir.absolutePath)) return path
    val dest = File(destDir, src.name)
    return if (src.renameTo(dest)) dest.absolutePath else {
        try {
            src.copyTo(dest, overwrite = true)
            src.delete()
            dest.absolutePath
        } catch (_: Exception) {
            path
        }
    }
}
