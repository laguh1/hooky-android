package com.hooky.app.ui.yarns.form

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.hooky.app.R
import com.hooky.app.ui.components.WordSafeText
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.White
import com.hooky.app.ui.yarns.YarnColorDot
import com.hooky.app.util.decodeSampledBitmapFromFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Full-screen photo: the user taps on the yarn and gets that exact colour,
// without the table or background that automatic detection can pick up.
@Composable
fun ColorFromPhotoDialog(
    photos: List<String>,
    onDismiss: () -> Unit,
    onPicked: (Int) -> Unit,
) {
    var photo by remember { mutableStateOf(photos.first()) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var marker by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(photo) {
        bitmap = null
        picked = null
        marker = null
        bitmap = withContext(Dispatchers.IO) { decodeSampledBitmapFromFile(photo, maxDimension = 1200) }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(
        usePlatformDefaultWidth = false,
        // Without this the dialog never receives the system bar sizes, so the bottom
        // buttons ended up behind a three-button navigation bar
        decorFitsSystemWindows = false
    )) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.yarn_color_from_photo_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = White,
                modifier = Modifier.padding(16.dp)
            )

            // Buttons at the top, where no navigation bar can cover them
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                picked?.let { YarnColorDot(it, size = 36.dp) }
                Box(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) {
                    WordSafeText(stringResource(R.string.action_cancel), style = LocalTextStyle.current.copy(color = White))
                }
                Button(
                    onClick = { picked?.let(onPicked) },
                    enabled = picked != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Slate,
                        disabledContainerColor = White.copy(alpha = 0.25f),
                        disabledContentColor = White.copy(alpha = 0.6f)
                    )
                ) {
                    WordSafeText(stringResource(R.string.action_confirm))
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val bmp = bitmap
                if (bmp == null) {
                    CircularProgressIndicator(color = White)
                } else {
                    val density = LocalDensity.current
                    val boxW = with(density) { maxWidth.toPx() }
                    val boxH = with(density) { maxHeight.toPx() }
                    // Where the photo sits inside the box with ContentScale.Fit
                    val scale = minOf(boxW / bmp.width, boxH / bmp.height)
                    val left = (boxW - bmp.width * scale) / 2f
                    val top = (boxH - bmp.height * scale) / 2f
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(bmp) {
                                detectTapGestures { tap ->
                                    val x = ((tap.x - left) / scale).toInt()
                                    val y = ((tap.y - top) / scale).toInt()
                                    if (x in 0 until bmp.width && y in 0 until bmp.height) {
                                        picked = averageColor(bmp, x, y)
                                        marker = tap
                                    }
                                }
                            }
                    )
                    marker?.let { m ->
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(Color.White, radius = 22.dp.toPx(), center = m, style = Stroke(3.dp.toPx()))
                            drawCircle(Color.Black, radius = 25.dp.toPx(), center = m, style = Stroke(1.dp.toPx()))
                        }
                    }
                }
            }

            // Other photos of this yarn, newest first
            if (photos.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(photos) { p ->
                        AsyncImage(
                            model = p,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(if (p == photo) 2.dp else 0.dp, White, RoundedCornerShape(8.dp))
                                .clickable { photo = p }
                        )
                    }
                }
            }

            // Some phones (Samsung, Android 16) report no navigation bar height inside this
            // dialog, so anything at the bottom could still sit under a three-button bar.
            // Keep a minimum gap at the bottom; the buttons live at the top (1.10.2).
            val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            Spacer(modifier = Modifier.height(maxOf(navBottom, 56.dp)))
        }
    }
}

// Colour of the yarn around the tap. Twisted yarn is full of small shadows, so a plain
// average comes out too dark: average only the lit half of the pixels (by brightness),
// leaving out the brightest few, which are shine rather than colour.
private fun averageColor(bmp: Bitmap, cx: Int, cy: Int): Int {
    val r = maxOf(4, minOf(bmp.width, bmp.height) / 40)
    val pixels = ArrayList<Int>((2 * r + 1) * (2 * r + 1))
    for (y in (cy - r)..(cy + r)) {
        if (y !in 0 until bmp.height) continue
        for (x in (cx - r)..(cx + r)) {
            if (x !in 0 until bmp.width) continue
            pixels += bmp.getPixel(x, y)
        }
    }
    fun luma(p: Int) = 0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)
    val sorted = pixels.sortedBy { luma(it) }
    val lit = sorted.subList(sorted.size / 2, maxOf(sorted.size / 2 + 1, sorted.size * 95 / 100))
    var red = 0L; var green = 0L; var blue = 0L
    lit.forEach { p -> red += (p shr 16) and 0xFF; green += (p shr 8) and 0xFF; blue += p and 0xFF }
    val n = lit.size
    return ((red / n).toInt() shl 16) or ((green / n).toInt() shl 8) or (blue / n).toInt()
}
