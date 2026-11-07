package com.hooky.app.util

/**
 * Parses a number typed by the user, accepting both "12,50" (PT/ES) and "12.50" (EN).
 *
 * `String.toFloatOrNull()` only understands a dot, so in Portuguese and Spanish every
 * price or cost typed with a comma used to be saved as empty (bug found 2026-11-03).
 *
 * When both separators appear ("1.234,56" or "1,234.56"), the last one is the decimal
 * separator and the other is a thousands separator. A single separator followed by exactly
 * three digits and nothing else is still read as a decimal ("1,500" → 1.5), because
 * prices, sizes and weights in this app are almost never in the thousands.
 */
fun String.toDecimalOrNull(): Float? {
    val s = trim().replace(" ", "").replace(" ", "")
    if (s.isEmpty()) return null
    val lastComma = s.lastIndexOf(',')
    val lastDot = s.lastIndexOf('.')
    val normalized = when {
        lastComma >= 0 && lastDot >= 0 -> {
            if (lastComma > lastDot) s.replace(".", "").replace(',', '.')
            else s.replace(",", "")
        }
        lastComma >= 0 -> {
            if (s.count { it == ',' } > 1) return null
            s.replace(',', '.')
        }
        else -> s
    }
    return normalized.toFloatOrNull()
}
