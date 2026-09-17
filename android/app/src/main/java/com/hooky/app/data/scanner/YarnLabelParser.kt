package com.hooky.app.data.scanner

import com.hooky.app.domain.model.YarnLabelScanResult
import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory

// A line of recognized text plus its height in pixels (from ML Kit's bounding box).
// heightPx = 0 when size info isn't available, which degrades gracefully to the
// original line-order heuristic (see extractBrandLine/extractName).
data class OcrLine(val text: String, val heightPx: Int)

object YarnLabelParser {

    fun parse(ocrText: String, ocrLines: List<OcrLine> = emptyList()): YarnLabelScanResult {
        val text = ocrText.trim()
        val lines = if (ocrLines.isNotEmpty()) ocrLines
            else text.lines().map { OcrLine(it.trim(), 0) }.filter { it.text.isNotBlank() }
        val brandLine = extractBrandLine(lines)
        val brand = brandLine?.text?.let { stripTrademarkSymbols(it) }?.let { correctKnownBrand(it) }

        return YarnLabelScanResult(
            name = extractName(lines, brandLine),
            brand = brand,
            colorName = extractColorName(text),
            colorCode = extractColorCode(text),
            material = extractMaterial(text),
            materialComposition = extractMaterialComposition(text),
            weightCategory = extractWeightCategory(text),
            ballWeightG = extractBallWeightG(text),
            ballLengthM = extractBallLengthM(text),
            hookSizeMm = extractHookSize(text),
            needleSizeMm = extractNeedleSize(text),
            gauge = extractGauge(text),
            washTemp = extractWashTemp(text),
            machineWash = extractMachineWash(text),
            handWash = extractHandWash(text),
            tumbleDry = extractTumbleDry(text),
            bleach = extractBleach(text),
            dryClean = extractDryClean(text)
        )
    }

    private val skipKeywords = setOf(
        "color", "colour", "cor", "col", "lot", "art", "weight", "categoria", "category",
        "peso", "grosor", "espesor", "grossura", "gramatura", "calibre",
        "lace", "dk", "worsted", "bulky", "chunky", "superchunky", "jumbo", "aran",
        "fingering", "sport", "ply", "plies", "fio", "fios", "hilo", "hilado", "novelo",
        "light", "medium", "heavy", "fine", "superfine", "extrafine",
        "lana", "lã", "algodón", "algodão", "acrilico", "acrílico",
        "wool", "cotton", "acrylic", "silk", "bamboo", "alpaca", "mohair",
        "made", "hecho", "feito", "fabricado", "product", "wash", "care", "lavado", "lavar",
        "machine", "hand", "iron", "dry", "tumble", "bleach"
    )

    // Strips leading/trailing punctuation and trademark/copyright symbols so keyword
    // matching isn't fooled by "(DK)", "DK:", "Katia®", etc.
    private fun normalizeWord(word: String): String =
        word.lowercase().trim { !it.isLetter() }

    private fun isUrlOrEmail(line: String): Boolean =
        line.contains("@") || Regex("""(?i)www\.|\.com\b|\.es\b|\.pt\b|\.br\b|\.co\.uk\b|https?://""").containsMatchIn(line)

    private fun stripTrademarkSymbols(value: String): String =
        value.replace(Regex("[®™©]"), "").trim()

    // Real, established yarn brands — used to auto-correct minor OCR misreads (dropped
    // or swapped letters are common on stylized/low-contrast label fonts, e.g. ML Kit
    // reading "VIKING GARN" as "VKING GARN"). Not exhaustive — extend as more brands
    // are reported.
    private val knownYarnBrands = listOf(
        "Katia", "DROPS", "Phildar", "Rico Design", "Schachenmayr", "Sirdar", "Stylecraft",
        "Sublime", "King Cole", "Hayfield", "Wendy", "James C Brett", "Debbie Bliss", "Rowan",
        "Sandnes Garn", "Viking Garn", "Viking", "Malabrigo", "Cascade Yarns", "Lion Brand",
        "Red Heart", "Bernat", "Caron", "Patons", "Premier Yarns", "Scheepjes", "Lang Yarns",
        "Novita", "Adriafil", "Anchor", "DMC", "Coats", "Círculo", "Bergère de France", "Regia",
        "West Yorkshire Spinners", "Pingouin", "LindeHobby", "Järbo", "Svarta", "Mondial",
        "Utopia Crafts", "Lana Gatto", "Coopay", "AUAUY", "Manos del Uruguay", "Hjertegarn",
        "Hoooked"
    )

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                    else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
        return dp[a.length][b.length]
    }

    // Matches a noisy OCR reading against knownYarnBrands within a length-scaled edit
    // distance. Returns the correctly-spelled/cased brand plus the match distance, or
    // null when nothing in the list is close enough — so this never forces a match
    // onto a genuinely unlisted brand.
    private fun bestKnownBrandMatch(raw: String): Pair<String, Int>? {
        val maxDistance = when {
            raw.length <= 8 -> 1
            raw.length <= 14 -> 2
            else -> 3
        }
        val bestMatch = knownYarnBrands.minByOrNull { levenshtein(it.lowercase(), raw.lowercase()) }
            ?: return null
        val distance = levenshtein(bestMatch.lowercase(), raw.lowercase())
        return if (distance <= maxDistance) bestMatch to distance else null
    }

    // Corrects a noisy OCR brand reading to its properly-spelled form when it's close
    // enough to something in knownYarnBrands. Falls back to the raw OCR text unchanged
    // otherwise, so this never overwrites a legitimate brand that isn't in the list yet.
    private fun correctKnownBrand(raw: String): String = bestKnownBrandMatch(raw)?.first ?: raw

    private fun isCandidateLine(text: String, lengthRange: IntRange): Boolean =
        text.length in lengthRange &&
        text.any { it.isLetter() } &&
        text.none { it.isDigit() } &&
        !isUrlOrEmail(text) &&
        text.split(Regex("\\s+")).none { normalizeWord(it) in skipKeywords }

    // Brand/trademark: prefers a line that closely matches a real, known yarn brand
    // regardless of its printed size on the label — many labels print the product
    // name or weight/type larger than the brand itself, so size alone picks the
    // wrong line more often than expected. Falls back to the tallest candidate line
    // (the original heuristic) only when no line is a close enough match to anything
    // known — this keeps unlisted brands working exactly as before.
    private fun extractBrandLine(lines: List<OcrLine>): OcrLine? {
        val candidates = lines.filter { isCandidateLine(it.text, 3..35) }
        if (candidates.isEmpty()) return null

        val knownMatch = candidates
            .mapNotNull { line -> bestKnownBrandMatch(stripTrademarkSymbols(line.text))?.let { line to it.second } }
            .minByOrNull { (_, distance) -> distance }

        return knownMatch?.first ?: candidates.maxByOrNull { it.heightPx }
    }

    // Name: the next-tallest candidate line after brand (the product/type name,
    // usually printed smaller than the brand but larger than material/care text).
    // Excludes the brand's own line by identity, not by string match, since the
    // brand string returned to callers has trademark symbols stripped already.
    private fun extractName(lines: List<OcrLine>, brandLine: OcrLine?): String? {
        return lines.filter { isCandidateLine(it.text, 4..50) && it != brandLine }
            .maxByOrNull { it.heightPx }
            ?.text?.let { stripTrademarkSymbols(it) }
    }

    // Color name: after color/colour/cor/col/farbe/coloris keywords
    private fun extractColorName(text: String): String? {
        val pattern = Regex(
            """(?i)(?:col(?:or(?:way|is)?|ou?r|\.)?|cor|farbe|colori?s?)[:\s]+([A-Za-zÀ-ÿ][A-Za-zÀ-ÿ\s\-]+?)(?:\s*[\d/\\|,]|${'$'})""",
            RegexOption.MULTILINE
        )
        return pattern.find(text)?.groupValues?.get(1)?.trim()
            ?.takeIf { it.length in 2..40 && it.any { c -> c.isLetter() } }
    }

    // Color code: standalone numeric/alphanumeric code near a color keyword
    private fun extractColorCode(text: String): String? {
        val pattern = Regex(
            """(?i)(?:col(?:or|ou?r)?\.?\s*(?:code|no\.?|num\.?|#|nr\.?)?\s*:?\s*)([A-Z]?\d{2,6}[A-Z]?)"""
        )
        return pattern.find(text)?.groupValues?.get(1)?.trim()
    }

    // Material: detect fiber keywords; if multiple fibers, return BLEND
    private fun extractMaterial(text: String): Material? {
        val lower = text.lowercase()
        val fiberMap = mapOf(
            "wool" to Material.WOOL,
            "merino" to Material.WOOL,
            "lana" to Material.WOOL,
            "lã" to Material.WOOL,
            "cotton" to Material.COTTON,
            "algodón" to Material.COTTON,
            "algodão" to Material.COTTON,
            "acrylic" to Material.ACRYLIC,
            "acrílico" to Material.ACRYLIC,
            "acrilico" to Material.ACRYLIC,
            "acrílica" to Material.ACRYLIC,
            "alpaca" to Material.ALPACA,
            "silk" to Material.SILK,
            "seda" to Material.SILK,
            "linen" to Material.LINEN,
            "linho" to Material.LINEN,
            "lino" to Material.LINEN,
            "bamboo" to Material.BAMBOO,
            "bambú" to Material.BAMBOO,
            "bambou" to Material.BAMBOO,
            "mohair" to Material.MOHAIR,
            "viscose" to Material.OTHER,
            "nylon" to Material.OTHER,
            "polyester" to Material.OTHER,
            "poliéster" to Material.OTHER
        )
        val detected = fiberMap.entries.filter { (keyword, _) -> keyword in lower }
            .map { it.value }
            .distinct()
        return when {
            detected.isEmpty() -> null
            detected.size == 1 -> detected.first()
            else -> Material.BLEND
        }
    }

    // Material composition: percentage patterns like "80% Merino Wool 20% Nylon"
    private fun extractMaterialComposition(text: String): String? {
        val pattern = Regex("""(\d{1,3}\s*%\s*[\w\s]+(?:\d{1,3}\s*%\s*[\w\s]+)*)""")
        return pattern.findAll(text)
            .map { it.value.trim() }
            .filter { it.length > 5 }
            .maxByOrNull { it.length }
            ?.trim()
    }

    // Weight category: standard English names + CYC numbers + Spanish/Portuguese terms
    private fun extractWeightCategory(text: String): WeightCategory? {
        val lower = text.lowercase()
        return when {
            Regex("""super[\s\-]?bulky|super[\s\-]?grueso|super[\s\-]?grosso|\bcyc\s*7\b""").containsMatchIn(lower) -> WeightCategory.SUPER_BULKY
            Regex("""\bbulky\b|\bgrueso\b|\bgrosso\b|\bchunky\b|\bcyc\s*[56]\b""").containsMatchIn(lower) -> WeightCategory.BULKY
            Regex("""\bworsted\b|\baran\b|\bmedio\b|\bmedium\b|\bcyc\s*4\b""").containsMatchIn(lower) -> WeightCategory.WORSTED
            Regex("""\bdk\b|\bdouble\s+knit\b|\bcyc\s*3\b""").containsMatchIn(lower) -> WeightCategory.DK
            Regex("""\bsport\b|\bbaby\b|\bcyc\s*2\b""").containsMatchIn(lower) -> WeightCategory.SPORT
            Regex("""\bfingering\b|\bsock\b|\bcyc\s*1\b""").containsMatchIn(lower) -> WeightCategory.FINGERING
            Regex("""\blace\b|\bcyc\s*0\b""").containsMatchIn(lower) -> WeightCategory.LACE
            else -> null
        }
    }

    // Ball weight in grams — also converts oz to g
    private fun extractBallWeightG(text: String): String? {
        val gramsPattern = Regex("""(\d{2,4})\s*g(?:r(?:ams?)?)?(?=\s|/|${'$'})""", RegexOption.IGNORE_CASE)
        val grams = gramsPattern.find(text)?.groupValues?.get(1)
        if (grams != null) return grams

        val ozPattern = Regex("""(\d+\.?\d*)\s*oz(?=\s|/|${'$'})""", RegexOption.IGNORE_CASE)
        return ozPattern.find(text)?.groupValues?.get(1)?.toFloatOrNull()
            ?.let { (it * 28.3495).toInt().toString() }
    }

    // Ball length in meters — also converts yards to meters
    private fun extractBallLengthM(text: String): String? {
        val metersPattern = Regex("""(\d{2,4})\s*m(?:etres?|eters?)?(?=\s|/|${'$'})""", RegexOption.IGNORE_CASE)
        val meters = metersPattern.find(text)?.groupValues?.get(1)
        if (meters != null) return meters

        val yardsPattern = Regex("""(\d{2,4})\s*y(?:ds?|ards?)(?=\s|/|${'$'})""", RegexOption.IGNORE_CASE)
        return yardsPattern.find(text)?.groupValues?.get(1)?.toIntOrNull()
            ?.let { (it * 0.9144).toInt().toString() }
    }

    // Hook size in mm (crochet hook) — handles European decimal comma
    private fun extractHookSize(text: String): String? {
        val hookPattern = Regex(
            """(?i)(?:crochet\s+)?(?:hook|ganchillo|crochê|gancho|griffe|haken)[:\s]*(\d+[.,]\d+|\d+)\s*mm"""
        )
        hookPattern.find(text)?.groupValues?.get(1)?.let { return it.replace(',', '.') }

        // US size / mm notation like "7/4.5mm" or "G/4mm"
        val slashPattern = Regex("""[A-Z\d]+/(\d+[.,]?\d*)\s*mm""")
        slashPattern.find(text)?.groupValues?.get(1)?.let { return it.replace(',', '.') }

        return null
    }

    // Needle size in mm (knitting needles) — handles Spanish/Portuguese and decimal comma
    private fun extractNeedleSize(text: String): String? {
        val pattern = Regex(
            """(?i)(?:needle[s]?|agujas?|agulha[s]?|aiguilles?)[:\s]*(\d+[.,]?\d*(?:\s*[-–]\s*\d+[.,]?\d*)?)\s*mm"""
        )
        return pattern.find(text)?.groupValues?.get(1)?.trim()?.replace(',', '.')
    }

    // Gauge: stitches per 10cm
    private fun extractGauge(text: String): String? {
        val pattern = Regex(
            """(?i)(\d+\s*(?:sts?|st\.|stitches?|puntos?|pontos?)[^.]{0,30}?=\s*\d+\s*cm)"""
        )
        return pattern.find(text)?.value?.trim()
    }

    // Wash temperature: "30°C", "wash at 30", "lavar a 30°"
    private fun extractWashTemp(text: String): String? {
        val pattern = Regex("""(?i)(?:wash(?:\s+at)?|lavar(?:\s+a)?|lavagem)\s*(?:at\s*)?(\d{2,3})\s*°?[Cc]?""")
        val fromContext = pattern.find(text)?.groupValues?.get(1)
        if (fromContext != null) return fromContext

        // Standalone temperature like "30°C" or "40°C"
        return Regex("""(\d{2,3})\s*°[Cc]""").find(text)?.groupValues?.get(1)
    }

    // Machine wash detection
    private fun extractMachineWash(text: String): Boolean? {
        val lower = text.lowercase()
        return when {
            Regex("""(?:do\s+not|não|no)\s+(?:machine\s+)?wash|no\s+lavar\s+a\s+m[aá]quina""").containsMatchIn(lower) -> false
            Regex("""machine\s+wash|lavar\s+a\s+m[aá]quina|lavado\s+a\s+m[aá]quina|lavagem\s+(?:na\s+)?m[aá]quina""").containsMatchIn(lower) -> true
            else -> null
        }
    }

    // Hand wash detection
    private fun extractHandWash(text: String): Boolean? {
        val lower = text.lowercase()
        return when {
            Regex("""hand\s+wash|lavar\s+a\s+mano|lavar\s+à\s+mão|lavagem\s+manual|lavado\s+a\s+mano""").containsMatchIn(lower) -> true
            else -> null
        }
    }

    // Tumble dry detection
    private fun extractTumbleDry(text: String): Boolean? {
        val lower = text.lowercase()
        return when {
            Regex("""(?:do\s+not|no|não|não)\s+tumble|no\s+(?:usar\s+)?secadora|não\s+(?:usar\s+)?secadora|no\s+secar\s+en\s+secadora""").containsMatchIn(lower) -> false
            Regex("""tumble\s+dry""").containsMatchIn(lower) -> true
            else -> null
        }
    }

    // Bleach detection (ISO triangle symbol) — "do not bleach" / "no bleach" vs.
    // any positive mention of bleach/lejía/alvejante/cloro
    private fun extractBleach(text: String): Boolean? {
        val lower = text.lowercase()
        return when {
            Regex("""(?:do\s+not|no|não)\s+bleach|no\s+usar\s+lej[ií]a|sin\s+lej[ií]a|não\s+usar\s+alvejante|sem\s+alvejante""").containsMatchIn(lower) -> false
            Regex("""\bbleach\b|lej[ií]a|alvejante|\bcloro\b""").containsMatchIn(lower) -> true
            else -> null
        }
    }

    // Professional/dry-clean detection (ISO circle symbol) — "do not dry clean" vs.
    // "dry clean only" / limpieza en seco / lavagem a seco
    private fun extractDryClean(text: String): Boolean? {
        val lower = text.lowercase()
        return when {
            Regex("""(?:do\s+not|no|não)\s+dry[\s-]?clean|no\s+(?:lavar|limpiar)\s+en\s+seco|não\s+lavar\s+a\s+seco""").containsMatchIn(lower) -> false
            Regex("""dry[\s-]?clean|limpieza\s+en\s+seco|lavado\s+en\s+seco|lavagem\s+a\s+seco|limpeza\s+a\s+seco""").containsMatchIn(lower) -> true
            else -> null
        }
    }
}
