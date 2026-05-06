package com.hooky.app.data.scanner

import androidx.core.graphics.ColorUtils
import kotlin.math.abs

/**
 * Maps an RGB color to a descriptive craft-friendly color name.
 * Uses HSL color space for perceptually meaningful buckets.
 * Color names are chosen to match typical yarn/textile naming conventions.
 */
object ColorNameMapper {

    fun toColorName(r: Int, g: Int, b: Int): String {
        val hsl = FloatArray(3)
        ColorUtils.RGBToHSL(r, g, b, hsl)
        val h = hsl[0]   // 0–360
        val s = hsl[1]   // 0–1
        val l = hsl[2]   // 0–1

        // 1. Achromatic (grey scale)
        if (s < 0.10f) return achromatic(l)

        // 2. Very light / pastel — check before hue
        if (l > 0.88f) return lightTone(h, s)

        // 3. Very dark — check before hue
        if (l < 0.18f) return darkTone(h)

        // 4. Named by hue + saturation + lightness
        return byHue(h, s, l)
    }

    private fun achromatic(l: Float): String = when {
        l > 0.96f -> "White"
        l > 0.88f -> "Off White"
        l > 0.75f -> "Light Grey"
        l > 0.55f -> "Grey"
        l > 0.35f -> "Dark Grey"
        l > 0.18f -> "Charcoal"
        else      -> "Black"
    }

    private fun lightTone(h: Float, s: Float): String {
        if (s < 0.20f) return "Cream"
        return when (h) {
            in 0f..20f, in 340f..360f -> "Blush"
            in 20f..55f               -> "Cream"
            in 55f..160f              -> "Mint"
            in 160f..250f             -> "Ice Blue"
            in 250f..310f             -> "Lilac"
            in 310f..340f             -> "Blush"
            else                      -> "Cream"
        }
    }

    private fun darkTone(h: Float): String = when (h) {
        in 0f..20f, in 340f..360f -> "Burgundy"
        in 20f..50f               -> "Brown"
        in 50f..160f              -> "Dark Green"
        in 160f..250f             -> "Navy"
        in 250f..310f             -> "Plum"
        in 310f..340f             -> "Plum"
        else                      -> "Dark"
    }

    private fun byHue(h: Float, s: Float, l: Float): String {
        return when {
            // ── Reds (0–15 and 345–360) ──────────────────────────────
            (h < 15f || h >= 345f) && l < 0.40f && s > 0.50f -> "Burgundy"
            (h < 15f || h >= 345f) && l in 0.40f..0.65f       -> "Red"
            (h < 15f || h >= 345f)                             -> "Coral Red"

            // ── Oranges / Earthy (15–45) ─────────────────────────────
            h in 15f..25f && s > 0.45f && l in 0.30f..0.55f -> "Rust"
            h in 15f..30f && s > 0.55f && l in 0.55f..0.75f -> "Coral"
            h in 25f..40f && s > 0.55f && l in 0.35f..0.55f -> "Terracotta"
            h in 30f..45f && s in 0.25f..0.55f && l in 0.55f..0.75f -> "Camel"
            h in 20f..45f && s > 0.65f                        -> "Orange"
            h in 20f..45f                                      -> "Warm Brown"

            // ── Yellows / Gold (45–65) ────────────────────────────────
            h in 45f..60f && s > 0.55f && l in 0.35f..0.55f -> "Mustard"
            h in 45f..60f && s > 0.55f && l in 0.55f..0.72f -> "Gold"
            h in 45f..65f                                      -> "Yellow"

            // ── Yellow-greens (65–90) ─────────────────────────────────
            h in 65f..90f && l < 0.40f  -> "Olive"
            h in 65f..90f               -> "Yellow Green"

            // ── Greens (90–160) ───────────────────────────────────────
            h in 90f..140f && s > 0.45f && l in 0.18f..0.35f -> "Forest Green"
            h in 90f..140f && s > 0.50f && l in 0.35f..0.55f -> "Green"
            h in 90f..140f && s in 0.15f..0.45f               -> "Sage"
            h in 90f..140f                                      -> "Mint Green"
            h in 140f..165f && s > 0.40f                       -> "Emerald"
            h in 140f..165f                                     -> "Sage"

            // ── Teals / Aquas (165–195) ───────────────────────────────
            h in 165f..185f && s > 0.50f && l > 0.50f -> "Turquoise"
            h in 165f..195f && s > 0.40f && l < 0.45f -> "Teal"
            h in 165f..195f                             -> "Teal"

            // ── Blues (195–250) ───────────────────────────────────────
            h in 195f..220f && s in 0.25f..0.55f -> "Steel Blue"
            h in 195f..225f && l > 0.55f          -> "Sky Blue"
            h in 215f..240f && l < 0.35f          -> "Navy"
            h in 195f..250f && s > 0.55f          -> "Blue"
            h in 195f..250f                        -> "Dusty Blue"

            // ── Purples (250–290) ─────────────────────────────────────
            h in 250f..270f && l > 0.60f           -> "Lavender"
            h in 250f..290f && l < 0.40f           -> "Deep Purple"
            h in 250f..290f && s > 0.55f           -> "Purple"
            h in 250f..290f                         -> "Dusty Purple"

            // ── Pinks / Magentas (290–345) ────────────────────────────
            h in 290f..325f && l < 0.40f           -> "Plum"
            h in 290f..325f && s in 0.15f..0.45f   -> "Mauve"
            h in 290f..325f                         -> "Pink"
            h in 325f..345f && s > 0.55f           -> "Hot Pink"
            h in 325f..345f && s in 0.20f..0.55f   -> "Dusty Rose"
            h in 325f..345f                         -> "Rose"

            else -> "Coloured"
        }
    }
}
