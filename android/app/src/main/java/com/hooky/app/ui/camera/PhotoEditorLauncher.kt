package com.hooky.app.ui.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Returns a lambda that, when called with a photo file path, launches the system photo editor.
 * On result: [onEditDone] receives (originalPath, editedPath).
 * If no editor app is available: [onNoEditor] is called instead.
 */
@Composable
fun rememberPhotoEditorLauncher(
    onEditDone: (originalPath: String, editedPath: String) -> Unit,
    onNoEditor: () -> Unit = {}
): (String) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentPath = remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val original = currentPath.value
            val resultUri = result.data?.data
            if (resultUri != null) {
                scope.launch {
                    val newPath = copyEditedPhoto(context, resultUri, original)
                    onEditDone(original, newPath ?: original)
                }
            } else {
                // Editor may have modified in-place; Coil reloads via File.lastModified() change
                onEditDone(original, original)
            }
        }
    }

    return { photoPath ->
        currentPath.value = photoPath
        val file = File(photoPath)
        if (file.exists()) {
            val editIntent = Intent(Intent.ACTION_EDIT).apply {
                val uri = FileProvider.getUriForFile(context, "com.hooky.app.fileprovider", file)
                setDataAndType(uri, "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            val resolved = context.packageManager.queryIntentActivities(editIntent, 0)
            if (resolved.isEmpty()) {
                onNoEditor()
            } else {
                launcher.launch(Intent.createChooser(editIntent, "Edit photo"))
            }
        }
    }
}

private suspend fun copyEditedPhoto(
    context: Context,
    uri: Uri,
    originalPath: String
): String? = withContext(Dispatchers.IO) {
    try {
        val dir = File(originalPath).parentFile
            ?: File(context.filesDir, "photos/temp").also { it.mkdirs() }
        val dest = File(dir, "edited_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        if (dest.exists() && dest.length() > 0) dest.absolutePath else null
    } catch (_: Exception) {
        null
    }
}
