package com.hooky.app.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.hooky.app.util.decodeSampledBitmapFromFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PhotoEditorScreen(
    photoPath: String,
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var flipHorizontal by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Photo preview with live rotation/flip applied via graphicsLayer
        AsyncImage(
            model = photoPath,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .graphicsLayer {
                    rotationZ = rotationDegrees.toFloat()
                    scaleX = if (flipHorizontal) -1f else 1f
                }
        )

        // Top bar: close + save
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        scope.launch {
                            val result = applyEditsAndSave(context, photoPath, rotationDegrees, flipHorizontal)
                            navController.previousBackStackEntry?.savedStateHandle?.apply {
                                set("original_photo_path", photoPath)
                                set("edited_photo_path", result ?: photoPath)
                            }
                            isSaving = false
                            onNavigateBack()
                        }
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Bottom controls: rotate left, flip, rotate right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditorButton(
                icon = { Icon(Icons.Filled.RotateLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) },
                label = "Rotate left",
                onClick = { rotationDegrees = (rotationDegrees - 90 + 360) % 360 }
            )
            EditorButton(
                icon = { Icon(Icons.Filled.Flip, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) },
                label = "Flip",
                onClick = { flipHorizontal = !flipHorizontal }
            )
            EditorButton(
                icon = { Icon(Icons.Filled.RotateRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) },
                label = "Rotate right",
                onClick = { rotationDegrees = (rotationDegrees + 90) % 360 }
            )
        }
    }
}

@Composable
private fun EditorButton(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) { icon() }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

private suspend fun applyEditsAndSave(
    context: Context,
    originalPath: String,
    rotation: Int,
    flipH: Boolean
): String? = withContext(Dispatchers.IO) {
    try {
        // Camera photos can be well above 4000px on one side — cap the decode at a size
        // still far sharper than anything this app displays (full-screen detail photos,
        // the 1080px share card) rather than holding a 40MB+ raw bitmap in memory.
        val src = decodeSampledBitmapFromFile(originalPath, maxDimension = 2048) ?: return@withContext null
        val matrix = Matrix().apply {
            if (flipH) postScale(-1f, 1f, src.width / 2f, src.height / 2f)
            if (rotation != 0) postRotate(rotation.toFloat())
        }
        val edited = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        src.recycle()
        val dir = File(originalPath).parentFile ?: return@withContext null
        val dest = File(dir, "edited_${System.currentTimeMillis()}.jpg")
        dest.outputStream().use { out -> edited.compress(Bitmap.CompressFormat.JPEG, 95, out) }
        edited.recycle()
        dest.absolutePath
    } catch (_: Exception) { null }
}
