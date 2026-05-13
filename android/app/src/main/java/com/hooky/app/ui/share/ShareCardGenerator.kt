package com.hooky.app.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.hooky.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ShareCardGenerator {

    suspend fun generateAndShare(
        context: Context,
        pieceName: String,
        pieceType: String?,
        destination: String?,
        workHours: Float?,
        rowCount: Int,
        photoPath: String?
    ): Intent? = withContext(Dispatchers.IO) {
        try {
            val bitmap = createCard(context, pieceName, pieceType, destination, workHours, rowCount, photoPath)
            val uri = saveToFilesDir(context, bitmap) ?: return@withContext null
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun createCard(
        context: Context,
        pieceName: String,
        pieceType: String?,
        destination: String?,
        workHours: Float?,
        rowCount: Int,
        photoPath: String?
    ): Bitmap {
        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = Color.parseColor("#1E293B")
        val accentColor = Color.parseColor("#8C015E")
        val photoZoneH = (size * 0.58f).toInt()

        canvas.drawColor(bgColor)

        if (photoPath != null) {
            val photo = BitmapFactory.decodeFile(photoPath)
            if (photo != null) {
                val srcAspect = photo.width.toFloat() / photo.height
                val dstAspect = size.toFloat() / photoZoneH
                val src = if (srcAspect > dstAspect) {
                    val w = (photo.height * dstAspect).toInt()
                    val x = (photo.width - w) / 2
                    Rect(x, 0, x + w, photo.height)
                } else {
                    val h = (photo.width / dstAspect).toInt()
                    val y = (photo.height - h) / 2
                    Rect(0, y, photo.width, y + h)
                }
                canvas.drawBitmap(photo, src, Rect(0, 0, size, photoZoneH), null)
                photo.recycle()
                val gradPaint = Paint()
                gradPaint.shader = LinearGradient(
                    0f, (photoZoneH - 160).toFloat(), 0f, photoZoneH.toFloat(),
                    Color.TRANSPARENT, bgColor, Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, (photoZoneH - 160).toFloat(), size.toFloat(), photoZoneH.toFloat(), gradPaint)
            }
        } else {
            val placeholderPaint = Paint().apply { color = Color.parseColor("#2D1B40") }
            canvas.drawRect(0f, 0f, size.toFloat(), photoZoneH.toFloat(), placeholderPaint)
        }

        // Accent left bar
        canvas.drawRect(0f, photoZoneH.toFloat(), 10f, size.toFloat(), Paint().apply { color = accentColor })

        val textX = 52f
        var y = photoZoneH + 72f

        // Piece name
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 80f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val displayName = if (pieceName.length > 24) pieceName.take(22) + "…" else pieceName
        canvas.drawText(displayName, textX, y, namePaint)
        y += 76f

        // Subtitle
        val sub = listOfNotNull(pieceType, destination).joinToString(" · ")
        if (sub.isNotEmpty()) {
            val subPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 42f
                isAntiAlias = true
            }
            canvas.drawText(sub, textX, y, subPaint)
            y += 64f
        }

        // Stats
        y += 20f
        val statBoldPaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val statLabelPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 34f
            isAntiAlias = true
        }
        var statX = textX
        if (workHours != null && workHours > 0f) {
            val hoursStr = if (workHours % 1f == 0f) "${workHours.toInt()}h" else "${"%.1f".format(workHours)}h"
            canvas.drawText(hoursStr, statX, y, statBoldPaint)
            canvas.drawText("Work time", statX, y + 44f, statLabelPaint)
            statX += 280f
        }
        if (rowCount > 0) {
            canvas.drawText("$rowCount", statX, y, statBoldPaint)
            canvas.drawText("Rows", statX, y + 44f, statLabelPaint)
        }

        // Hooky wordmark (bottom right)
        try {
            val wm = BitmapFactory.decodeResource(context.resources, R.drawable.hooky_wordmark)
            if (wm != null) {
                val wmH = 64
                val wmW = (wm.width * wmH.toFloat() / wm.height).toInt()
                val scaled = Bitmap.createScaledBitmap(wm, wmW, wmH, true)
                canvas.drawBitmap(scaled, (size - wmW - 44).toFloat(), (size - wmH - 44).toFloat(), Paint().apply { alpha = 200 })
                scaled.recycle()
                wm.recycle()
            }
        } catch (_: Exception) {}

        return bitmap
    }

    private fun saveToFilesDir(context: Context, bitmap: Bitmap): Uri? = try {
        val shareDir = File(context.filesDir, "share").also { it.mkdirs() }
        val file = File(shareDir, "hooky_piece.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        FileProvider.getUriForFile(context, "com.hooky.app.fileprovider", file)
    } catch (_: Exception) {
        null
    }
}
