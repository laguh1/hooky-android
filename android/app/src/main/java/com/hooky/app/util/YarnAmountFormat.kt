package com.hooky.app.util

import java.text.NumberFormat
import kotlin.math.abs
import kotlin.math.floor

// Crafters think in "2 and a third balls", so common fractions are shown as
// fraction glyphs (2⅓) and anything else as a number in the user's locale (2,4 / 2.4).
private val FRACTIONS = listOf(
    1f / 4 to "¼", 1f / 3 to "⅓", 1f / 2 to "½", 2f / 3 to "⅔", 3f / 4 to "¾"
)

fun formatBalls(balls: Float): String {
    val whole = floor(balls + 0.005f)
    val part = balls - whole
    if (abs(part) < 0.005f) return whole.toInt().toString()
    FRACTIONS.firstOrNull { abs(it.first - part) < 0.01f }?.let { (_, glyph) ->
        return if (whole == 0f) glyph else "${whole.toInt()}$glyph"
    }
    return NumberFormat.getNumberInstance().apply { maximumFractionDigits = 2 }.format(balls)
}

/**
 * For totals (dashboard stash): a sum of fractions like 13⅙ isn't something crafters count in,
 * and showed as "13,17". Rounds to the nearest whole, quarter or third and marks it with "≈"
 * when that isn't exact.
 */
fun formatBallsApprox(balls: Float): String {
    val whole = floor(balls)
    val part = balls - whole
    val steps = listOf(0f, 1f / 4, 1f / 3, 1f / 2, 2f / 3, 3f / 4, 1f)
    val nearest = steps.minByOrNull { abs(it - part) }!!
    val rounded = whole + nearest
    val text = formatBalls(rounded)
    return if (abs(rounded - balls) < 0.01f) text else "≈ $text"
}

// Accepts "2", "2.5", "2,5", "1/3", "2 1/3" and "2⅓". Returns null for anything else.
fun parseBalls(text: String): Float? {
    var s = text.trim()
    if (s.isEmpty()) return null
    FRACTIONS.forEach { (value, glyph) ->
        if (s.endsWith(glyph)) {
            val whole = s.removeSuffix(glyph).trim()
            return (if (whole.isEmpty()) 0f else whole.toFloatOrNull() ?: return null) + value
        }
    }
    s = s.replace(',', '.')
    val parts = s.split(Regex("\\s+"))
    val fraction = parts.last().split('/')
    if (fraction.size == 2) {
        val num = fraction[0].toFloatOrNull() ?: return null
        val den = fraction[1].toFloatOrNull()?.takeIf { it != 0f } ?: return null
        val whole = if (parts.size == 2) parts[0].toFloatOrNull() ?: return null else if (parts.size == 1) 0f else return null
        return whole + num / den
    }
    return if (parts.size == 1) s.toFloatOrNull() else null
}
