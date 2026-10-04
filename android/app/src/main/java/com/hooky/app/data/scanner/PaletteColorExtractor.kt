package com.hooky.app.data.scanner

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.hooky.app.util.decodeSampledBitmapFromFile
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
                // Palette only needs a representative sample of pixels, not full resolution —
                // decoding small avoids the costly full-size decode entirely.
                val bitmap = decodeSampledBitmapFromFile(photoPath, maxDimension = 400) ?: return@withContext null
                val palette = Palette.from(bitmap).generate()

                // Yarn close-ups are fuzzy/textured, so the single biggest pixel cluster
                // (dominantSwatch) is often a desaturated shadow/highlight blend rather
                // than the yarn's true color — which is why this used to come back
                // "Grey" for most yarns. Prefer the most saturated swatch among every
                // candidate Palette found, and only fall back to dominant/largest when
                // nothing has meaningful saturation (i.e. the yarn genuinely is neutral).
                val hsl = FloatArray(3)
                fun saturationOf(rgb: Int): Float {
                    ColorUtils.colorToHSL(rgb, hsl)
                    return hsl[1]
                }

                val candidates = listOfNotNull(
                    palette.vibrantSwatch,
                    palette.lightVibrantSwatch,
                    palette.darkVibrantSwatch,
                    palette.mutedSwatch,
                    palette.lightMutedSwatch,
                    palette.darkMutedSwatch
                )
                val mostSaturated = candidates.maxByOrNull { saturationOf(it.rgb) }

                val swatch = mostSaturated?.takeIf { saturationOf(it.rgb) >= 0.15f }
                    ?: palette.dominantSwatch
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
