package com.hooky.app.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PhotoStorageUtil {

    /**
     * Move a photo from the temp directory to an entity-specific directory.
     * e.g. filesDir/photos/pieces/PIECE-001/photo_timestamp.jpg
     *
     * @param context      Application or activity context
     * @param tempPath     Absolute path of the temp file
     * @param entityType   "pieces", "yarns", or "stitches"
     * @param entityId     e.g. "PIECE-001"
     * @return             New absolute path after the move
     */
    suspend fun movePhotoToEntity(
        context: Context,
        tempPath: String,
        entityType: String,
        entityId: String
    ): String = withContext(Dispatchers.IO) {
        val tempFile = File(tempPath)
        require(tempFile.exists()) { "Temp photo does not exist: $tempPath" }

        val destDir = File(context.filesDir, "photos/$entityType/$entityId")
        ensureDir(destDir)

        val destFile = File(destDir, tempFile.name)
        tempFile.copyTo(destFile, overwrite = true)
        tempFile.delete()

        destFile.absolutePath
    }

    /**
     * Delete a photo file from storage.
     *
     * @param path Absolute file path
     * @return     true if deleted successfully (or already absent), false on error
     */
    fun deletePhoto(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else true
        } catch (_: Exception) {
            false
        }
    }

    private fun ensureDir(dir: File) {
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }
}
