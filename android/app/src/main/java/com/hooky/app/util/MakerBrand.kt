package com.hooky.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * The maker's own brand for share cards (1.11): shop name or @handle and a logo, set once in
 * Settings. Free for everyone. Pro can also hide the small "made with Hooky App" mark.
 */
data class MakerBrand(val name: String, val logoPath: String?, val hideHookyMark: Boolean) {
    val isEmpty: Boolean get() = name.isBlank() && logoPath == null

    companion object {
        private const val KEY_NAME = "makerBrandName"
        private const val KEY_HIDE_MARK = "makerBrandHideHookyMark"
        private const val LOGO_DIR = "brand"
        private const val LOGO_FILE = "logo.png"
        private const val LOGO_MAX_PX = 512

        private fun logoFile(ctx: Context) = File(File(ctx.filesDir, LOGO_DIR), LOGO_FILE)

        fun load(ctx: Context): MakerBrand {
            val p = AppPrefs.get(ctx)
            val logo = logoFile(ctx).takeIf { it.exists() }?.absolutePath
            return MakerBrand(p.getString(KEY_NAME, "").orEmpty(), logo, p.getBoolean(KEY_HIDE_MARK, false))
        }

        fun saveName(ctx: Context, name: String) =
            AppPrefs.get(ctx).edit().putString(KEY_NAME, name.trim().take(40)).apply()

        fun setHideHookyMark(ctx: Context, hide: Boolean) =
            AppPrefs.get(ctx).edit().putBoolean(KEY_HIDE_MARK, hide).apply()

        /** Copies the picked image into app storage, scaled down, as PNG (keeps transparency). */
        fun saveLogo(ctx: Context, uri: Uri): Boolean = try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= LOGO_MAX_PX) sample *= 2
            val decoded = ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: error("cannot decode")
            val scale = LOGO_MAX_PX.toFloat() / maxOf(decoded.width, decoded.height)
            val logo = if (scale < 1f)
                Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)
            else decoded
            val file = logoFile(ctx).also { it.parentFile?.mkdirs() }
            file.outputStream().use { logo.compress(Bitmap.CompressFormat.PNG, 100, it) }
            true
        } catch (_: Exception) {
            false
        }

        fun removeLogo(ctx: Context) {
            logoFile(ctx).delete()
        }
    }
}
