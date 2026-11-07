package com.hooky.app.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import com.hooky.app.util.MakerBrand
import com.hooky.app.util.decodeSampledBitmapFromFile
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
        photoPath: String?,
        isPremium: Boolean = false
    ): Intent? = withContext(Dispatchers.IO) {
        try {
            val brand = MakerBrand.load(context)
            val bitmap = createCard(context, pieceName, pieceType, destination, workHours, rowCount, photoPath, brand, isPremium)
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
        photoPath: String?,
        brand: MakerBrand,
        isPremium: Boolean
    ): Bitmap {
        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = Color.parseColor("#1E293B")
        val accentColor = Color.parseColor("#8C015E")
        val photoZoneH = (size * 0.58f).toInt()

        canvas.drawColor(bgColor)

        if (photoPath != null) {
            val photo = decodeSampledBitmapFromFile(photoPath, maxDimension = 1200)
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
            val hoursStr = com.hooky.app.util.formatWorkTime(workHours)
            val label = context.getString(R.string.share_card_work_time)
            canvas.drawText(hoursStr, statX, y, statBoldPaint)
            canvas.drawText(label, statX, y + 44f, statLabelPaint)
            // ES/PT labels are wider than "Work time", so space by the text
            statX += maxOf(280f, statBoldPaint.measureText(hoursStr) + 60f, statLabelPaint.measureText(label) + 60f)
        }
        if (rowCount > 0) {
            canvas.drawText("$rowCount", statX, y, statBoldPaint)
            canvas.drawText(context.getString(R.string.share_card_rows), statX, y + 44f, statLabelPaint)
        }

        val margin = 44
        val bottom = size - margin

        // "via Hooky App" (bottom right): the app icon + text. Not "made with", which reads as
        // if the piece itself was made by Hooky (user, 2026-11-07). Pro can hide it.
        val showHookyMark = !(isPremium && brand.hideHookyMark)
        var markLeft = size - margin
        if (showHookyMark) try {
            val big = brand.isEmpty
            val icon = if (big) 60 else 48
            val viaPaint = Paint().apply {
                color = Color.WHITE; alpha = 220; textSize = if (big) 36f else 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true
            }
            val label = context.getString(R.string.share_card_via)
            val textW = viaPaint.measureText(label)
            val textX = size - margin - textW
            val iconL = textX - 14 - icon
            val iconT = (bottom - icon).toFloat()
            // App icon: brand-purple rounded square with the white "oo"
            canvas.drawRoundRect(iconL, iconT, iconL + icon, iconT + icon, icon * 0.24f, icon * 0.24f,
                Paint().apply { color = accentColor; isAntiAlias = true })
            androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_notification)?.let { d ->
                val pad = (icon * 0.16f).toInt()
                d.setBounds(iconL.toInt() + pad, iconT.toInt() + pad, (iconL + icon).toInt() - pad, (iconT + icon).toInt() - pad)
                d.draw(canvas)
            }
            val fm = viaPaint.fontMetrics
            val baseline = iconT + icon / 2f - (fm.ascent + fm.descent) / 2f
            canvas.drawText(label, textX, baseline, viaPaint)
            markLeft = iconL.toInt()
        } catch (_: Exception) {}

        // The maker's brand (bottom left): logo on a white tile + shop name / @handle
        if (!brand.isEmpty) {
            val tile = 112
            var x = 52f
            brand.logoPath?.let { path ->
                decodeSampledBitmapFromFile(path, maxDimension = 256)?.let { logo ->
                    val top = (bottom - tile).toFloat()
                    canvas.drawRoundRect(x, top, x + tile, top + tile, 20f, 20f, Paint().apply { color = Color.WHITE; isAntiAlias = true })
                    val inner = tile - 16
                    val s = minOf(inner.toFloat() / logo.width, inner.toFloat() / logo.height)
                    val w = logo.width * s; val h = logo.height * s
                    val l = x + (tile - w) / 2; val t = top + (tile - h) / 2
                    canvas.drawBitmap(logo, null, android.graphics.RectF(l, t, l + w, t + h), Paint().apply { isFilterBitmap = true })
                    logo.recycle()
                    x += tile + 24
                }
            }
            if (brand.name.isNotBlank()) {
                val namePaintBrand = Paint().apply {
                    color = Color.WHITE; textSize = 44f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
                }
                val maxW = markLeft - 32 - x
                var text = brand.name
                while (text.length > 1 && namePaintBrand.measureText(text) > maxW) text = text.dropLast(2) + "…"
                val baseline = if (brand.logoPath != null) bottom - tile / 2f + 16f else bottom - 6f
                canvas.drawText(text, x, baseline, namePaintBrand)
            }
        }

        return bitmap
    }

    private fun saveToFilesDir(context: Context, bitmap: Bitmap): Uri? = try {
        val shareDir = File(context.filesDir, "share").also { it.mkdirs() }
        val file = File(shareDir, "hooky_piece.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
        null
    }
}
