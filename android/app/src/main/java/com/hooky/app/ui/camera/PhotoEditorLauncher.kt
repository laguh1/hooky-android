package com.hooky.app.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import java.io.File
import java.net.URLEncoder

@Composable
fun rememberPhotoEditorLauncher(
    navController: NavController,
    onEditDone: (originalPath: String, editedPath: String) -> Unit
): (String) -> Unit {
    LaunchedEffect(navController) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow("edited_photo_path", "")
            ?.collect { edited ->
                if (edited.isNotBlank()) {
                    val original = navController.currentBackStackEntry
                        ?.savedStateHandle?.get<String>("original_photo_path") ?: edited
                    onEditDone(original, edited)
                    navController.currentBackStackEntry?.savedStateHandle?.set("edited_photo_path", "")
                }
            }
    }

    return { photoPath ->
        if (File(photoPath).exists()) {
            val encoded = URLEncoder.encode(photoPath, "UTF-8")
            navController.navigate("photo_editor/$encoded")
        }
    }
}
