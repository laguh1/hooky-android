package com.hooky.app.data.scanner

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.palette.graphics.Palette
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class ExtractedColor(
    val name: String,   // e.g. "Dusty Rose"
    val rgb: Int        // Android Color int for preview swatch
)

@Singleton
class PaletteColorExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Extracts the dominant color from a photo and returns a craft-friendly color name.
     * Returns null if the image cannot be decoded or palette is empty.
     */
    suspend fun extractFromPath(photoPath: String): ExtractedColor? =
        withContext(Dispatchers.Default) {
            try {
                val bitmap = BitmapFactory.decodeFile(photoPath) ?: return@withContext null
                val palette = Palette.from(bitmap).generate()

                // Priority: dominant → vibrant → muted → first swatch
                val swatch = palette.dominantSwatch
                    ?: palette.vibrantSwatch
                    ?: palette.mutedSwatch
                    ?: palette.swatches.maxByOrNull { it.population }
                    ?: return@withContext null

                val rgb = swatch.rgb
                val r = Color.red(rgb)
                val g = Color.green(rgb)
                val b = Color.blue(rgb)

                val name = ColorNameMapper.toColorName(r, g, b)
                ExtractedColor(name = name, rgb = rgb)
            } catch (_: Exception) {
                null
            }
        }
}
