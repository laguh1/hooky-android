package com.hooky.app.ui.camera

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Returns a lambda that, when invoked, launches the system photo picker (Android 13+)
 * or a generic image GET_CONTENT picker on older devices.
 *
 * The selected image is copied to [context.filesDir]/photos/temp/ and the absolute
 * path is returned via [onPhotoSelected].
 */
@Composable
fun rememberPhotoPickerLauncher(
    onPhotoSelected: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+: use PickVisualMedia (Photo Picker)
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                val path = copyUriToTemp(context, uri)
                if (path != null) onPhotoSelected(path)
            }
        }
        ({
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        })
    } else {
        // API < 33: fall back to GetContent
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                val path = copyUriToTemp(context, uri)
                if (path != null) onPhotoSelected(path)
            }
        }
        ({ launcher.launch("image/*") })
    }
}

/**
 * Copy the content at [uri] into the app's private temp photo directory and
 * return the absolute path, or null if the copy failed.
 */
private suspend fun copyUriToTemp(
    context: Context,
    uri: android.net.Uri
): String? = withContext(Dispatchers.IO) {
    try {
        val tempDir = File(context.filesDir, "photos/temp").also { it.mkdirs() }
        val destFile = File(tempDir, "photo_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (destFile.exists() && destFile.length() > 0) destFile.absolutePath else null
    } catch (_: Exception) {
        null
    }
}
