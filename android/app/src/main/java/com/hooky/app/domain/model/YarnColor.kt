package com.hooky.app.domain.model

import java.text.Normalizer

// Yarn colours as 0xRRGGBB ints, stored as "#RRGGBB" in YarnEntity.colorHex.
// Yarns saved before the colour itself was stored only have a typed name, so
// colourForName() turns common names (PT-BR, ES, EN, plus the detector's English
// names) into a colour for the circle. Unknown names give null: no circle, no guess.
object YarnColor {

    /** Swatches offered in the yarn form, in rainbow order, then browns and neutrals. */
    val PALETTE: List<Int> = listOf(
        0xD32F2F, 0x800020, 0xE91E63, 0xF48FB1, 0xB5838D, 0xFF7F50, 0xFB8C00, 0xE2725B,
        0xFDD835, 0xD4A017, 0xC5E384, 0x43A047, 0x9CAF88, 0x808000, 0x40E0D0, 0x008080,
        0x87CEEB, 0x1E88E5, 0x1F2A5A, 0xC8A2C8, 0x8E24AA, 0x6A2C70, 0x795548, 0xC19A6B,
        0xF5F5DC, 0xF3EEDC, 0xFFFFFF, 0xD3D3D3, 0x9E9E9E, 0x36454F, 0x000000,
    )

    fun toHex(rgb: Int): String = "#%06X".format(rgb and 0xFFFFFF)

    fun parseHex(hex: String?): Int? {
        val s = hex?.trim()?.removePrefix("#") ?: return null
        if (s.length != 6) return null
        return s.toIntOrNull(16)
    }

    /** The colour to show: the saved one, else one worked out from the name. */
    fun effective(colorHex: String?, colorName: String?): Int? =
        parseHex(colorHex) ?: colorName?.let { colourForName(it) }

    fun colourForName(name: String): Int? {
        val words = normalise(name).split(' ', '-', '/', ',').filter { it.isNotBlank() }
        if (words.isEmpty()) return null
        // Two-word names first ("dusty rose", "verde agua"), then single words
        val pair = words.zipWithNext { a, b -> "$a $b" }.firstOrNull { it in NAMES }
        var base = pair?.let { NAMES.getValue(it) } ?: words.firstNotNullOfOrNull { NAMES[it] } ?: return null
        // "claro"/"dark" lighten or darken, unless already part of the name ("light grey")
        val modifiers = words - (pair?.split(' ')?.toSet() ?: emptySet())
        when {
            modifiers.any { it in LIGHTER } -> base = mix(base, 0xFFFFFF, 0.4f)
            modifiers.any { it in DARKER } -> base = mix(base, 0x000000, 0.35f)
        }
        return base
    }

    private fun normalise(s: String): String =
        Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").trim()

    private fun mix(a: Int, b: Int, t: Float): Int {
        fun ch(c: Int, shift: Int) = (c shr shift) and 0xFF
        fun lerp(x: Int, y: Int) = (x + (y - x) * t).toInt().coerceIn(0, 255)
        return (lerp(ch(a, 16), ch(b, 16)) shl 16) or (lerp(ch(a, 8), ch(b, 8)) shl 8) or lerp(ch(a, 0), ch(b, 0))
    }

    private val LIGHTER = setOf("claro", "clara", "light", "pastel", "bebe", "baby", "pale")
    private val DARKER = setOf("escuro", "escura", "oscuro", "oscura", "dark", "deep", "fechado")

    private val NAMES: Map<String, Int> = buildMap {
        fun add(rgb: Int, vararg names: String) = names.forEach { put(it, rgb) }
        add(0xD32F2F, "vermelho", "rojo", "red", "encarnado")
        add(0xE91E63, "rosa", "pink", "rose")
        add(0xF48FB1, "blush", "rosado", "rosinha")
        add(0xC2185B, "pink choque", "hot pink", "fucsia", "fuchsia", "magenta")
        add(0xB5838D, "dusty rose", "rosa antigo", "rosa palo", "malva", "mauve")
        add(0xFB8C00, "laranja", "naranja", "orange")
        add(0xFF7F50, "coral", "salmao", "salmon")
        add(0xFDD835, "amarelo", "amarillo", "yellow")
        add(0xD4A017, "mostarda", "mostaza", "mustard", "ocre", "ochre")
        add(0xC9A227, "dourado", "dorado", "gold", "ouro", "oro")
        add(0x43A047, "verde", "green")
        add(0x98FF98, "mint", "menta", "mint green", "verde menta")
        add(0x9CAF88, "sage", "salvia", "verde salvia", "sage green", "verde oliva claro")
        add(0x808000, "olive", "oliva", "verde oliva", "musgo")
        add(0x228B22, "forest green", "verde bandeira", "verde bosque")
        add(0x50C878, "emerald", "esmeralda")
        add(0x66CDAA, "verde agua", "aqua", "agua marinha", "aguamarina")
        add(0x40E0D0, "turquesa", "turquoise", "tiffany")
        add(0x008080, "teal", "petroleo", "verde petroleo", "azul petroleo")
        add(0x1E88E5, "azul", "blue")
        add(0x87CEEB, "sky blue", "azul celeste", "celeste", "azul ceu", "azul bebe")
        add(0x6B8BA4, "dusty blue", "azul acinzentado", "steel blue", "azul aco", "jeans", "denim")
        add(0xBFE6F5, "ice blue", "azul gelo")
        add(0x1F2A5A, "navy", "marinho", "azul marinho", "marino", "azul marino")
        add(0x8E24AA, "roxo", "morado", "purple", "violeta", "violet", "purpura", "uva")
        add(0xC8A2C8, "lilas", "lila", "lilac")
        add(0xB57EDC, "lavanda", "lavender", "lavanda")
        add(0x6A2C70, "plum", "ameixa", "berenjena", "berinjela", "deep purple", "dusty purple")
        add(0x800020, "vinho", "burgundy", "bordo", "bordeaux", "burdeos", "granate", "marsala", "cereja")
        add(0x795548, "marrom", "marron", "brown", "cafe", "chocolate", "castanho", "warm brown")
        add(0xC19A6B, "camel", "caramelo", "caramel", "tostado", "canela")
        add(0xB7410E, "rust", "ferrugem", "oxido", "cobre", "copper")
        add(0xE2725B, "terracota", "terracotta", "telha")
        add(0xF5F5DC, "bege", "beige", "areia", "arena", "sand", "nude", "camurca")
        add(0xFFFDD0, "creme", "crema", "cream", "marfim", "marfil", "ivory")
        add(0xF3EEDC, "cru", "crudo", "ecru", "natural", "off white", "perola", "perla")
        add(0xFFFFFF, "branco", "blanco", "white")
        add(0x000000, "preto", "negro", "black")
        add(0x9E9E9E, "cinza", "gris", "grey", "gray", "mescla")
        add(0xD3D3D3, "light grey", "cinza claro", "gris claro", "prata", "plateado", "silver")
        add(0x36454F, "charcoal", "grafite", "grafito", "chumbo", "dark grey", "cinza escuro", "gris oscuro")
        add(0xC5E384, "yellow green", "verde limao", "verde lima", "lima", "lime")
    }
}

/** Colour groups for filtering, in rainbow order (the order chips and "sort by colour" use). */
// Filter-circle swatches: muted, yarn-like tones (1.11, user request) instead of saturated
// primaries. Only the circle colour; the grouping in of() is unchanged.
enum class ColorFamily(val swatch: Int) {
    RED(0xC75B5B), PINK(0xD98A9F), ORANGE(0xD9894A), YELLOW(0xE3C065), GREEN(0x7FA58A),
    BLUE(0x6F8FBF), PURPLE(0x9A82B8), BROWN(0xA88468), NEUTRAL(0xE9E4DA);

    companion object {
        fun of(rgb: Int): ColorFamily {
            val (h, s, l) = hsl(rgb)
            return when {
                // white, cream, ecru, beige (very light), greys, black
                s < 0.18f || l > 0.88f || l < 0.10f -> NEUTRAL
                // dark or muted warm tones read as brown (camel, chocolate, rust)
                h in 15f..50f && (l < 0.45f || s < 0.45f) -> BROWN
                h < 15f || h >= 345f -> if (l > 0.70f) PINK else RED
                h < 45f -> ORANGE
                h < 70f -> YELLOW
                h < 170f -> GREEN
                h < 260f -> BLUE
                h < 320f -> PURPLE     // lilac sits right at 300°
                else -> PINK
            }
        }

        /** Sort key for "sort by colour": group first, then hue, then light to dark. */
        fun sortKey(rgb: Int?): Double {
            if (rgb == null) return Double.MAX_VALUE        // no colour: at the end
            val (h, _, l) = hsl(rgb)
            return of(rgb).ordinal * 1000.0 + h + (1 - l)
        }

        private fun hsl(rgb: Int): Triple<Float, Float, Float> {
            val r = ((rgb shr 16) and 0xFF) / 255f
            val g = ((rgb shr 8) and 0xFF) / 255f
            val b = (rgb and 0xFF) / 255f
            val max = maxOf(r, g, b); val min = minOf(r, g, b)
            val l = (max + min) / 2f
            if (max == min) return Triple(0f, 0f, l)
            val d = max - min
            val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
            val h = when (max) {
                r -> ((g - b) / d + (if (g < b) 6 else 0)) * 60f
                g -> ((b - r) / d + 2) * 60f
                else -> ((r - g) / d + 4) * 60f
            }
            return Triple(h, s, l)
        }
    }
}
