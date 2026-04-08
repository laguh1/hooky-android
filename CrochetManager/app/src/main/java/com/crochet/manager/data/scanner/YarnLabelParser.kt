package com.crochet.manager.data.scanner

import com.crochet.manager.domain.model.YarnLabelScanResult
import com.crochet.manager.domain.model.enums.Material
import com.crochet.manager.domain.model.enums.WeightCategory

object YarnLabelParser {

    fun parse(ocrText: String): YarnLabelScanResult {
        val text = ocrText.trim()
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        return YarnLabelScanResult(
            brand = extractBrand(lines),
            colorName = extractColorName(text),
            colorCode = extractColorCode(text),
            material = extractMaterial(text),
            materialComposition = extractMaterialComposition(text),
            weightCategory = extractWeightCategory(text),
            ballWeightG = extractBallWeightG(text),
            ballLengthM = extractBallLengthM(text),
            hookSizeMm = extractHookSize(text),
            needleSizeMm = extractNeedleSize(text),
            gauge = extractGauge(text)
        )
    }

    // Brand: first short line with no digits and no label keywords
    private fun extractBrand(lines: List<String>): String? {
        val skipKeywords = setOf(
            "color", "colour", "cor", "col", "lot", "art", "weight",
            "lace", "dk", "worsted", "bulky", "fingering", "sport",
            "lana", "lã", "algodón", "algodão", "acrilico", "acrílico",
            "wool", "cotton", "acrylic", "silk", "bamboo", "alpaca", "mohair",
            "made", "product", "wash", "care", "lavado", "lavar"
        )
        return lines.firstOrNull { line ->
            line.length in 3..35 &&
            line.any { it.isLetter() } &&
            line.none { it.isDigit() } &&
            line.split(Regex("\\s+")).none { it.lowercase() in skipKeywords }
        }
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
            "alpaca" to Material.ALPACA,
            "silk" to Material.SILK,
            "seda" to Material.SILK,
            "linen" to Material.LINEN,
            "linho" to Material.LINEN,
            "lino" to Material.LINEN,
            "bamboo" to Material.BAMBOO,
            "bambú" to Material.BAMBOO,
            "bambú" to Material.BAMBOO,
            "mohair" to Material.MOHAIR
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

    // Weight category: standard names + CYC numbers
    private fun extractWeightCategory(text: String): WeightCategory? {
        val lower = text.lowercase()
        return when {
            Regex("""super[\s\-]?bulky|\bcyc\s*7\b""").containsMatchIn(lower) -> WeightCategory.SUPER_BULKY
            Regex("""\bbulky\b|\bcyc\s*[56]\b""").containsMatchIn(lower) -> WeightCategory.BULKY
            Regex("""\bworsted\b|\baran\b|\bcyc\s*4\b""").containsMatchIn(lower) -> WeightCategory.WORSTED
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

    // Hook size in mm (crochet hook)
    private fun extractHookSize(text: String): String? {
        // Explicit hook keyword
        val hookPattern = Regex(
            """(?i)(?:crochet\s+)?(?:hook|ganchillo|crochê|gancho)[:\s]*(\d+\.?\d*)\s*mm"""
        )
        hookPattern.find(text)?.groupValues?.get(1)?.let { return it }

        // Slash notation like "7/4.5mm" (US size / mm)
        val slashPattern = Regex("""[A-Z\d]+/(\d+\.?\d*)\s*mm""")
        slashPattern.find(text)?.groupValues?.get(1)?.let { return it }

        return null
    }

    // Needle size in mm (knitting needles)
    private fun extractNeedleSize(text: String): String? {
        val pattern = Regex(
            """(?i)needle[s]?[:\s]*(\d+\.?\d*(?:\s*[-–]\s*\d+\.?\d*)?)\s*mm"""
        )
        return pattern.find(text)?.groupValues?.get(1)?.trim()
    }

    // Gauge: stitches per 10cm
    private fun extractGauge(text: String): String? {
        val pattern = Regex(
            """(?i)(\d+\s*(?:sts?|st\.|stitches?|puntos?|pontos?)[^.]{0,30}?=\s*\d+\s*cm)"""
        )
        return pattern.find(text)?.value?.trim()
    }
}
