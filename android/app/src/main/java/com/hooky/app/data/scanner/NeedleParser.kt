package com.hooky.app.data.scanner

import com.hooky.app.domain.model.NeedleScanResult

object NeedleParser {

    // US crochet hook letter/number → mm
    private val usCrochetHookMm = mapOf(
        "B" to "2.25", "1" to "2.25",
        "C" to "2.75", "2" to "2.75",
        "D" to "3.25", "3" to "3.25",
        "E" to "3.5",  "4" to "3.5",
        "F" to "3.75", "5" to "3.75",
        "G" to "4.0",  "6" to "4.0",
        "7" to "4.5",
        "H" to "5.0",  "8" to "5.0",
        "I" to "5.5",  "9" to "5.5",
        "J" to "6.0",  "10" to "6.0",
        "K" to "6.5",
        "L" to "8.0",  "11" to "8.0",
        "M" to "9.0",  "N" to "9.0",  "13" to "9.0",
        "P" to "10.0", "Q" to "10.0", "15" to "10.0"
    )

    // US knitting needle number → mm
    private val usKnittingNeedleMm = mapOf(
        "0" to "2.0",
        "1" to "2.25",
        "2" to "2.75",
        "3" to "3.25",
        "4" to "3.5",
        "5" to "3.75",
        "6" to "4.0",
        "7" to "4.5",
        "8" to "5.0",
        "9" to "5.5",
        "10" to "6.0",
        "10.5" to "6.5",
        "11" to "8.0",
        "13" to "9.0",
        "15" to "10.0",
        "17" to "12.75",
        "19" to "15.0",
        "35" to "19.0",
        "50" to "25.0"
    )

    // UK/Canadian needle → mm
    private val ukNeedleMm = mapOf(
        "14" to "2.0",
        "13" to "2.25",
        "12" to "2.75",
        "11" to "3.0",
        "10" to "3.25",
        "9"  to "3.75",
        "8"  to "4.0",
        "7"  to "4.5",
        "6"  to "5.0",
        "5"  to "5.5",
        "4"  to "6.0",
        "3"  to "6.5",
        "2"  to "7.0",
        "1"  to "7.5",
        "0"  to "8.0",
        "00" to "9.0",
        "000" to "10.0"
    )

    fun parse(ocrText: String): NeedleScanResult? {
        val text = ocrText.trim()
        if (text.isBlank()) return null

        // 1. Direct mm value — most reliable (e.g. "4.5mm", "4,5mm", "4.5 mm")
        val mmPattern = Regex("""(\d+[.,]\d+|\d+)\s*mm""", RegexOption.IGNORE_CASE)
        mmPattern.find(text)?.let { match ->
            val size = match.groupValues[1].replace(',', '.')
            return NeedleScanResult(sizeMm = size, rawMarking = text)
        }

        // 2. Slash notation — crochet hook (e.g. "G/6", "H/8", "K/10.5")
        val slashPattern = Regex("""([A-Z])/(\d+\.?\d*)""")
        slashPattern.find(text.uppercase())?.let { match ->
            val letter = match.groupValues[1]
            val mm = usCrochetHookMm[letter]
            if (mm != null) return NeedleScanResult(sizeMm = mm, rawMarking = match.value)
        }

        // 3. US crochet hook letter only (e.g. "G", "H", "J")
        val letterOnlyPattern = Regex("""(?<![A-Z])([B-N]|[PQ])(?![A-Z])""")
        letterOnlyPattern.find(text.uppercase())?.let { match ->
            val letter = match.groupValues[1]
            val mm = usCrochetHookMm[letter]
            if (mm != null) return NeedleScanResult(sizeMm = mm, rawMarking = match.value)
        }

        // 4. US knitting needle (e.g. "US 7", "US 10.5")
        val usPattern = Regex("""(?i)US\s*(\d+\.?\d*)""")
        usPattern.find(text)?.let { match ->
            val num = match.groupValues[1]
            val mm = usKnittingNeedleMm[num]
            if (mm != null) return NeedleScanResult(sizeMm = mm, rawMarking = match.value)
        }

        // 5. UK needle (e.g. "UK 7", "7 UK")
        val ukPattern = Regex("""(?i)(?:UK\s*(\d+)|(\d+)\s*UK)""")
        ukPattern.find(text)?.let { match ->
            val num = (match.groupValues[1].ifBlank { match.groupValues[2] })
            val mm = ukNeedleMm[num]
            if (mm != null) return NeedleScanResult(sizeMm = mm, rawMarking = match.value)
        }

        // 6. Bare number — last resort, only if short text (likely just the size engraved)
        if (text.length <= 6) {
            val barePattern = Regex("""^(\d+[.,]?\d*)$""")
            barePattern.find(text.trim())?.let { match ->
                val size = match.groupValues[1].replace(',', '.')
                val num = size.toFloatOrNull()
                if (num != null && num in 1.5f..25.0f) {
                    return NeedleScanResult(sizeMm = size, rawMarking = text)
                }
            }
        }

        return null
    }
}
