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
import com.hooky.app.domain.model.enums.NeedleType
import com.hooky.app.util.MakerBrand
import com.hooky.app.util.decodeSampledBitmapFromFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** A yarn as the share card shows it: name big, "Yarn · brand" small. */
data class ShareCardYarn(val name: String, val brand: String?)

/** A hook or needle size for the share card; [type] picks the label (Hook / Needles). */
data class ShareCardTool(val sizeMm: Float, val type: NeedleType?)

object ShareCardGenerator {

    // Tools that set the piece's gauge; tapestry and cable needles never describe the work.
    private val gaugeTools = setOf(NeedleType.CROCHET_HOOK, NeedleType.KNITTING_NEEDLE, NeedleType.DPN, NeedleType.CIRCULAR)

    /**
     * The hook/needle to show: the first linked one that sets the gauge, then any linked one,
     * then the piece's own hook size (often empty, so the linked needles come first).
     */
    fun pickTool(linked: List<ShareCardTool>, pieceHookSizeMm: Float?): ShareCardTool? =
        linked.firstOrNull { it.type in gaugeTools }
            ?: linked.firstOrNull { it.type != NeedleType.TAPESTRY_NEEDLE && it.type != NeedleType.CABLE_NEEDLE }
            ?: pieceHookSizeMm?.takeIf { it > 0f }?.let { ShareCardTool(it, NeedleType.CROCHET_HOOK) }

    /** "4,5 mm" in PT/ES, "4.5 mm" in EN; no ".0" on whole sizes. */
    fun formatSize(mm: Float, locale: java.util.Locale = java.util.Locale.getDefault()): String {
        val nf = java.text.NumberFormat.getNumberInstance(locale)
        nf.maximumFractionDigits = 2
        nf.isGroupingUsed = false
        return nf.format(mm.toDouble()) + " mm"
    }

    suspend fun generateAndShare(
        context: Context,
        pieceName: String,
        pieceType: String?,
        destination: String?,
        yarns: List<ShareCardYarn>,
        tool: ShareCardTool?,
        photoPath: String?,
        isPremium: Boolean = false
    ): Intent? = withContext(Dispatchers.IO) {
        try {
            val brand = MakerBrand.load(context)
            val bitmap = createCard(context, pieceName, pieceType, destination, yarns, tool, photoPath, brand, isPremium)
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
        yarns: List<ShareCardYarn>,
        tool: ShareCardTool?,
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
        // Stats: the yarn (what buyers and followers ask about) and the hook/needle size.
        // Replaced work time + rows in 1.12 (user, 2026-11-12); a stat with no data is hidden.
        val rightEdge = size - 52f
        var toolW = 0f
        var toolValue: String? = null
        var toolLabel: String? = null
        if (tool != null) {
            toolValue = formatSize(tool.sizeMm)
            toolLabel = context.getString(
                if (tool.type == NeedleType.CROCHET_HOOK || tool.type == null) R.string.share_card_hook else R.string.share_card_needles
            )
            toolW = maxOf(statBoldPaint.measureText(toolValue), statLabelPaint.measureText(toolLabel))
        }
        val firstYarn = yarns.firstOrNull()
        if (firstYarn != null) {
            val more = if (yarns.size > 1) " +${yarns.size - 1}" else ""
            val yarnLabel = firstYarn.brand?.let { context.getString(R.string.share_card_yarn_brand, it) }
                ?: context.getString(R.string.share_card_yarn)
            val maxW = (if (toolValue != null) rightEdge - toolW - 60f else rightEdge) - textX
            val yarnPaint = Paint(statBoldPaint)
            // Long yarn names shrink a little before being cut with "…"
            while (yarnPaint.measureText(firstYarn.name + more) > maxW && yarnPaint.textSize > 42f) yarnPaint.textSize -= 2f
            canvas.drawText(ellipsize(firstYarn.name, more, yarnPaint, maxW), textX, y, yarnPaint)
            canvas.drawText(ellipsize(yarnLabel, "", statLabelPaint, maxW), textX, y + 44f, statLabelPaint)
        }
        if (toolValue != null && toolLabel != null) {
            // Right-aligned when next to a yarn, otherwise in the yarn's place
            val x = if (firstYarn != null) rightEdge - toolW else textX
            canvas.drawText(toolValue, x, y, statBoldPaint)
            canvas.drawText(toolLabel, x, y + 44f, statLabelPaint)
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

    // Cuts [text] with "…" so text + suffix fits [maxW]; the suffix ("+2") is always kept.
    private fun ellipsize(text: String, suffix: String, paint: Paint, maxW: Float): String {
        if (paint.measureText(text + suffix) <= maxW) return text + suffix
        var t = text
        while (t.length > 1 && paint.measureText(t.trimEnd() + "…" + suffix) > maxW) t = t.dropLast(1)
        return t.trimEnd() + "…" + suffix
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
