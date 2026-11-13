package com.hooky.app.data.scanner

import com.hooky.app.domain.model.YarnLabelScanResult
import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory
import java.text.Normalizer

// A line of recognized text plus its height in pixels (from ML Kit's bounding box) and the
// text block it came from. heightPx = 0 when size info isn't available, which degrades
// gracefully to line order; block = -1 when unknown (lines are then never joined).
data class OcrLine(val text: String, val heightPx: Int, val block: Int = -1)

/**
 * Turns the OCR text of a yarn label into form fields. Reworked in 1.12 after testing five
 * real labels (2026-11-13): name, brand and colour code were mostly wrong — script logos and
 * icon captions ("Knitting Needle Size") taken as the name, a marketing sentence as the
 * colour, barcode digits as the colour code. Tests: YarnLabelParserTest.
 */
object YarnLabelParser {

    fun parse(ocrText: String, ocrLines: List<OcrLine> = emptyList()): YarnLabelScanResult {
        val text = ocrText.trim()
        val lines = (if (ocrLines.isNotEmpty()) ocrLines
            else text.lines().map { OcrLine(it.trim(), 0) })
            .map { it.copy(text = it.text.trim()) }
            .filter { it.text.isNotBlank() }
        val brandPick = extractBrand(lines, text)
        val composition = extractMaterialComposition(lines)

        return YarnLabelScanResult(
            name = extractName(lines, brandPick),
            brand = brandPick?.brand,
            colorName = extractColorName(text),
            colorCode = extractColorCode(text, lines),
            // The composition line is the most reliable source; care text and marketing
            // copy elsewhere on the label mention other fibres ("wool cycle").
            material = composition?.let { extractMaterial(it) } ?: extractMaterial(text),
            materialComposition = composition,
            weightCategory = extractWeightCategory(text),
            ballWeightG = extractBallWeightG(text),
            ballLengthM = extractBallLengthM(text),
            // The yarn's hook size is one number in the database: a label range ("5-5,5 mm")
            // would be saved as empty, so the smaller size is used
            hookSizeMm = extractHookSize(text)?.substringBefore('-'),
            needleSizeMm = extractNeedleSize(text, lines),
            gauge = extractGauge(text),
            washTemp = extractWashTemp(text),
            machineWash = extractMachineWash(text),
            handWash = extractHandWash(text),
            tumbleDry = extractTumbleDry(text),
            bleach = extractBleach(text),
            dryClean = extractDryClean(text)
        )
    }

    /**
     * Fills fields from a second OCR pass of the photo turned 180° (stickers are often stuck
     * on upside down, e.g. "COL 122 / LOT 24F08"). Only numeric/technical fields are taken:
     * upside-down passes also read junk, which must never become a name or brand.
     */
    fun fillFromRotated(primary: YarnLabelScanResult, rotated: YarnLabelScanResult): YarnLabelScanResult =
        primary.copy(
            colorCode = primary.colorCode ?: rotated.colorCode,
            ballWeightG = primary.ballWeightG ?: rotated.ballWeightG,
            ballLengthM = primary.ballLengthM ?: rotated.ballLengthM,
            hookSizeMm = primary.hookSizeMm ?: rotated.hookSizeMm,
            needleSizeMm = primary.needleSizeMm ?: rotated.needleSizeMm,
            materialComposition = primary.materialComposition ?: rotated.materialComposition,
            material = primary.material ?: rotated.material,
            weightCategory = primary.weightCategory ?: rotated.weightCategory
        )

    // ---------------------------------------------------------------- words and helpers

    // Words that only appear in field captions, icons and care text. A line with any of
    // them is never the name or brand ("Knitting Needle Size", "Yarn Weight", "Colour").
    private val fieldLabelWords = setOf(
        "color", "colour", "colours", "colors", "cor", "col", "lot", "lote", "art", "shade",
        "weight", "peso", "categoria", "category", "grosor", "espesor", "grossura", "gramatura",
        "size", "sizes", "needle", "needles", "hook", "hooks", "aguja", "agujas", "agulha", "agulhas",
        "ganchillo", "gancho", "tamanho", "tamaño", "length", "largo", "longitud", "comprimento",
        "yarn", "knitting", "knit", "crochet", "crochê", "tricô", "tricot", "punto",
        "made", "hecho", "feito", "fabricado", "product", "producto", "produto",
        "wash", "care", "lavado", "lavar", "machine", "hand", "iron", "dry", "tumble", "bleach",
        "oeko", "tex", "gauge", "tension", "muestra", "amostra", "composition", "composición",
        "composição", "meters", "metres", "metros", "grams", "gramos", "gramas", "barcode",
        "www", "http", "https", "approx", "aprox"
    )

    // Weight and fibre words: fine inside a name ("Merino Bliss", "Perfect DK") but a line
    // made only of them is a description, not a name.
    private val genericWords = setOf(
        "lace", "dk", "worsted", "bulky", "chunky", "superchunky", "jumbo", "aran", "fingering",
        "sport", "ply", "plies", "fio", "fios", "hilo", "hilado", "novelo", "ovillo",
        "light", "medium", "heavy", "fine", "superfine", "extrafine", "super", "baby",
        "lana", "lã", "algodón", "algodão", "acrilico", "acrílico", "acrylic", "wool", "cotton",
        "silk", "seda", "bamboo", "alpaca", "mohair", "merino", "superwash", "nylon", "polyamide",
        "poliamida", "viscose", "linen", "linho", "lino", "100", "virgin", "new", "pure", "puro", "pura"
    )

    // Words that turn a line into a sentence ("A luxurious blend of brushed alpaca and silk").
    private val sentenceWords = setOf(
        "a", "an", "the", "of", "and", "with", "for", "in", "on", "is", "this", "that", "your",
        "de", "del", "la", "el", "los", "las", "con", "para", "por", "en", "y", "un", "una",
        "com", "e", "em", "do", "da", "dos", "das", "um", "uma", "o", "os", "as", "seu", "sua"
    )

    private fun stripAccents(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

    private fun normalizeWord(word: String): String =
        word.lowercase().trim { !it.isLetterOrDigit() }

    private fun words(line: String): List<String> =
        line.split(Regex("[\\s/|·•,;:]+")).map { normalizeWord(it) }.filter { it.isNotEmpty() }

    // Field words, also when OCR dropped or changed a letter ("NITTING" from "knitting",
    // "Neede" from "needle"): a word of 5+ letters within one edit of a field word.
    private fun isFieldWord(w: String): Boolean {
        if (w in fieldLabelWords) return true
        if (w.length < 5) return false
        return fieldLabelWords.any { it.length >= 5 && kotlin.math.abs(it.length - w.length) <= 1 && levenshtein(it, w) <= 1 }
    }

    private fun isUrlOrEmail(line: String): Boolean =
        line.contains("@") || Regex("""(?i)www\.|\.com\b|\.es\b|\.pt\b|\.br\b|\.co\.uk\b|https?://""").containsMatchIn(line)

    private fun stripTrademarkSymbols(value: String): String =
        value.replace(Regex("[®™©]"), "").trim()

    // A measurement or count: "50g", "175 m", "100%", "4,5 mm", "10 x 10 cm".
    private val measurement = Regex("""(?i)\d\s*(?:%|g\b|gr\b|grs\b|m\b|mts?\b|mm\b|cm\b|oz\b|yds?\b)""")

    // ---------------------------------------------------------------- brand

    // Real, established yarn brands — used to auto-correct OCR misreads (dropped, swapped or
    // accent letters are common on stylized logos, e.g. "VKING GARN", "CARICIi"). Not
    // exhaustive — extend as more brands are reported.
    private val knownYarnBrands = listOf(
        "Katia", "DROPS", "Phildar", "Rico Design", "Schachenmayr", "Sirdar", "Stylecraft",
        "Sublime", "King Cole", "Hayfield", "Wendy", "James C Brett", "Debbie Bliss", "Rowan",
        "Sandnes Garn", "Viking Garn", "Viking", "Malabrigo", "Cascade Yarns", "Lion Brand",
        "Red Heart", "Bernat", "Caron", "Patons", "Premier Yarns", "Scheepjes", "Lang Yarns",
        "Novita", "Adriafil", "Anchor", "DMC", "Coats", "Círculo", "Bergère de France", "Regia",
        "West Yorkshire Spinners", "Pingouin", "LindeHobby", "Järbo", "Svarta", "Mondial",
        "Utopia Crafts", "Lana Gatto", "Coopay", "AUAUY", "Manos del Uruguay", "Hjertegarn",
        "Hoooked", "EuroRoma", "Linhas Corrente", "Corrente", "Carícia", "Cisne", "Rosários 4",
        "Lanas Stop", "Hilaturas LM", "Paintbox Yarns", "Hobbii", "Durable", "Lana Grossa", "Fibra Natura"
    )

    // A distinctive word printed alone on some labels (logo split over two lines).
    private val brandAliases = mapOf(
        "utopia" to "Utopia Crafts", "linde" to "LindeHobby", "scheepjes" to "Scheepjes",
        "stylecraft" to "Stylecraft", "paintbox" to "Paintbox Yarns", "hobbii" to "Hobbii"
    )

    private fun key(s: String) = stripAccents(s).lowercase().replace(Regex("[^a-z0-9 ]"), "").trim()

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

    private fun maxBrandDistance(len: Int) = when {
        len <= 4 -> 0
        len <= 8 -> 1
        len <= 14 -> 2
        else -> 3
    }

    // Matches a noisy OCR reading against knownYarnBrands (accents and case ignored) within a
    // length-scaled edit distance. Returns the correctly-spelled brand plus the distance, or
    // null when nothing is close enough — never forces a match onto an unlisted brand.
    private fun bestKnownBrandMatch(raw: String): Pair<String, Int>? {
        val k = key(raw)
        if (k.length < 3) return null
        brandAliases[k]?.let { return it to 0 }
        val best = knownYarnBrands.minByOrNull { levenshtein(key(it), k) } ?: return null
        val distance = levenshtein(key(best), k)
        return if (distance <= maxBrandDistance(k.length)) best to distance else null
    }

    private class BrandPick(val brand: String, val line: OcrLine?)

    // Brand: a line that matches a known brand wins, whatever its size (labels often print
    // the product name bigger). Then a known brand written anywhere in the text. Last, the
    // tallest plausible line — keeps unlisted brands working as before.
    private fun extractBrand(lines: List<OcrLine>, text: String): BrandPick? {
        val known = lines.filter { it.text.length in 3..35 && !isUrlOrEmail(it.text) }
            .mapNotNull { line -> bestKnownBrandMatch(stripTrademarkSymbols(line.text))?.let { Triple(line, it.first, it.second) } }
            .minByOrNull { it.third }
        if (known != null) return BrandPick(known.second, known.first)

        val flat = " " + key(text.replace('\n', ' ')) + " "
        (knownYarnBrands.map { it to key(it) } + brandAliases.map { (alias, brand) -> brand to alias })
            .firstOrNull { (_, k) -> k.length >= 4 && flat.contains(" $k ") }
            ?.let { return BrandPick(it.first, null) }

        val fallback = lines.filter { isNameLike(it.text, 3..35) }.maxByOrNull { it.heightPx } ?: return null
        return BrandPick(tidyCase(stripTrademarkSymbols(fallback.text)), fallback)
    }

    // ---------------------------------------------------------------- name

    // Could this line be a product name? Digits are fine ("Merino Bliss 175"), captions,
    // measurements, sentences and pure fibre/weight lines are not.
    private fun isNameLike(text: String, lengthRange: IntRange = 3..40, allowGeneric: Boolean = false): Boolean {
        val t = stripTrademarkSymbols(text)
        if (t.length !in lengthRange || isUrlOrEmail(t)) return false
        val letters = t.count { it.isLetter() }
        if (letters < 3 || t.count { it.isDigit() } > letters) return false
        if (measurement.containsMatchIn(t)) return false
        val ws = words(t)
        if (ws.isEmpty() || ws.size > 5) return false
        if (ws.any { isFieldWord(it) }) return false
        if (ws.all { it in genericWords || it.all(Char::isDigit) } && !(allowGeneric && ws.size >= 2)) return false
        if (ws.size >= 3 && ws.any { it in sentenceWords }) return false
        return true
    }

    // A line that can continue a name on the next line ("Brushed" / "Alpaca Silk").
    private fun canContinueName(text: String): Boolean {
        val t = stripTrademarkSymbols(text)
        if (t.length > 25 || isUrlOrEmail(t) || measurement.containsMatchIn(t)) return false
        val ws = words(t)
        return ws.isNotEmpty() && ws.size <= 3 && ws.none { isFieldWord(it) || it in sentenceWords } &&
            t.count { it.isLetter() } >= 2
    }

    // Is [line] (very likely) the brand logo read badly ("Lndeobiy" for "LindeHobby")?
    private fun looksLikeBrand(line: String, brand: String): Boolean {
        val a = key(line).replace(" ", ""); val b = key(brand).replace(" ", "")
        if (a.isEmpty() || b.isEmpty()) return false
        if (a == b || a.contains(b) || (a.length >= 4 && b.contains(a))) return true
        return levenshtein(a, b) <= (maxOf(a.length, b.length) * 0.45).toInt()
    }

    // Name: the tallest name-like line that isn't the brand, joined with the next line of the
    // same block when it has a similar size (names often wrap: "PERFECT" / "DK").
    private fun extractName(lines: List<OcrLine>, brand: BrandPick?): String? {
        fun pick(allowGeneric: Boolean) = lines.filter { line ->
            line != brand?.line && isNameLike(line.text, allowGeneric = allowGeneric) &&
                (brand == null || !looksLikeBrand(line.text, brand.brand))
        }.maxByOrNull { it.heightPx }
        // A name made only of fibre/weight words ("Merino Aran") is the fallback when no
        // other line qualifies
        val best = pick(false) ?: pick(true) ?: return null
        var name = stripTrademarkSymbols(best.text)
        val i = lines.indexOf(best)
        val next = lines.getOrNull(i + 1)
        if (next != null && best.block >= 0 && next.block == best.block && best.heightPx > 0 &&
            next.heightPx >= best.heightPx * 0.7 && next.heightPx <= best.heightPx * 1.4 &&
            next != brand?.line && canContinueName(next.text) &&
            (brand == null || !looksLikeBrand(next.text, brand.brand))
        ) {
            name += " " + stripTrademarkSymbols(next.text)
        }
        return tidyCase(name)
    }

    // "PERFECT DK" → "Perfect DK", "MERINO BLISS 175" → "Merino Bliss 175": all-caps words
    // of 4+ letters get title case, short ones (DK, 4PLY) stay. Mixed-case text is kept.
    private fun tidyCase(s: String): String {
        if (s.any { it.isLowerCase() }) return s
        return s.split(" ").joinToString(" ") { w ->
            if (w.count { it.isLetter() } >= 4) w.lowercase().replaceFirstChar { it.titlecase() } else w
        }
    }

    // ---------------------------------------------------------------- colour

    // Colour name: after a colour keyword, on the same line, and short (never a sentence:
    // "colour\nA luxurious blend of brushed" gave a whole slogan as the colour).
    private fun extractColorName(text: String): String? {
        val pattern = Regex(
            """(?im)\b(?:colou?r(?:way)?|cor|farbe|coloris|colori)\b[ \t]*[:.\-]?[ \t]*([A-Za-zÀ-ÿ][A-Za-zÀ-ÿ \t\-]*?)[ \t]*(?:[\d/\\|,(]|$)"""
        )
        return pattern.findAll(text).map { it.groupValues[1].trim() }.firstOrNull { c ->
            val ws = words(c)
            c.length in 2..25 && ws.size in 1..3 && ws.none { it in sentenceWords || isFieldWord(it) }
        }
    }

    // A colour code: "122", "UC1000", "LH-SHI-01", "D-17". Has a digit; not a barcode.
    private val codeToken = """[A-Z0-9]{1,6}(?:-[A-Z0-9]{1,6}){0,3}"""

    private fun isCode(code: String): Boolean =
        code.length >= 2 && code.any { it.isDigit() } && !Regex("""\d{7,}""").containsMatchIn(code)

    private val colourKeyword = """(?:col|colou?r|cor|shade|farbe|coloris|colore|tono|nuance)"""

    // Colour code: a code right after a colour keyword on the same line — "COL 122",
    // "Colour: UC1000", "Shade 24", "Cor nº 0451" — or, when the keyword is a caption on its
    // own line ("Colour:" above "LH-SHI-01", LindeHobby Shine 2026-11-13), the first code on
    // the next line. Never barcode digits, never a weight or length ("Color 50g").
    private fun extractColorCode(text: String, lines: List<OcrLine>): String? {
        val pattern = Regex(
            """(?im)\b$colourKeyword\b\.?[ \t]*(?:code|n[º°o]\.?|num\.?|nr\.?|#)?[ \t]*[:.\-]?[ \t]*(?:([A-Za-zÀ-ÿ]{2,20})[ \t]+)?($codeToken|[A-Z]{1,3}[ \t]\d{1,6})\b(?![ \t]*(?:g|gr|m|mm|cm|%|oz|yds?)\b)"""
        )
        pattern.findAll(text)
            // One colour name may sit between keyword and code ("Color: Rojo 52"), never a
            // field word ("Colour Lot 1210" is the lot)
            .filter { m -> m.groupValues[1].isEmpty() || !isFieldWord(normalizeWord(m.groupValues[1])) }
            .map { it.groupValues[2].replace(Regex("[ \t]"), "").uppercase() }
            .firstOrNull { isCode(it) }
            ?.let { return it }

        // Caption line: only the keyword, maybe with a neighbouring caption ("Colour: Dyelot")
        val caption = Regex("""(?i)^\s*$colourKeyword\s*(?:code|n[º°o]\.?)?\s*:?\s*(?:(?:dye\s*)?lot\w*\s*:?)?\s*$""")
        val otherCaption = Regex("""(?i)^\s*(?:dye\s*)?lot[e\w]*\s*(?:n[º°o]\.?)?\s*:?\s*$|^\s*bain\s*:?\s*$|^\s*partida\s*:?\s*$""")
        for ((i, line) in lines.withIndex()) {
            if (!caption.matches(line.text)) continue
            // The code is on the next line, or one further down when OCR read the neighbouring
            // caption in between ("Colour:" / "Dyelot" / "LH-SHI-01 1025")
            for (next in lines.drop(i + 1).take(2).map { it.text.trim() }) {
                if (otherCaption.matches(next)) continue
                // A barcode line ("5 714215 064256") is never a colour code
                if (next.count { it.isDigit() } >= 10 && next.none { it.isLetter() }) break
                val first = Regex(codeToken).find(next.uppercase())?.value
                if (first != null && isCode(first) && next.uppercase().startsWith(first)) return first
                break
            }
        }
        return null
    }

    // ---------------------------------------------------------------- material

    private val fiberMap = mapOf(
        "wool" to Material.WOOL, "merino" to Material.WOOL, "lana" to Material.WOOL, "lã" to Material.WOOL,
        "cotton" to Material.COTTON, "algodón" to Material.COTTON, "algodão" to Material.COTTON, "algodon" to Material.COTTON,
        "acrylic" to Material.ACRYLIC, "acrílico" to Material.ACRYLIC, "acrilico" to Material.ACRYLIC, "acrílica" to Material.ACRYLIC,
        "alpaca" to Material.ALPACA, "silk" to Material.SILK, "seda" to Material.SILK,
        "linen" to Material.LINEN, "linho" to Material.LINEN, "lino" to Material.LINEN,
        "bamboo" to Material.BAMBOO, "bambú" to Material.BAMBOO, "bambou" to Material.BAMBOO,
        "mohair" to Material.MOHAIR,
        "viscose" to Material.OTHER, "nylon" to Material.OTHER, "polyester" to Material.OTHER,
        "poliéster" to Material.OTHER, "polyamide" to Material.OTHER, "poliamida" to Material.OTHER
    )

    // Material: fibre keywords; several fibres → BLEND. Bilingual labels name one fibre twice
    // ("100% Acrílico / Acrylic"), which is still one material.
    private fun extractMaterial(text: String): Material? {
        val lower = text.lowercase()
        val detected = fiberMap.entries.filter { (k, _) -> Regex("""(?<![\p{L}])${Regex.escape(k)}""").containsMatchIn(lower) }
            .map { it.value }.distinct()
        return when {
            detected.isEmpty() -> null
            detected.size == 1 -> detected.first()
            else -> Material.BLEND
        }
    }

    private val compositionWords = fiberMap.keys + setOf(
        "superwash", "extrafine", "extra", "fine", "virgin", "new", "baby", "brushed", "combed",
        "mercerized", "mercerised", "mercerizado", "peinado", "penteado", "elastane", "elastano",
        "polyamid", "acrylique", "laine", "coton", "soie", "wolle", "baumwolle"
    )

    // Composition: only the "NN% fibre" lines, cleaned ("100%Acrylic" → "100% Acrylic"),
    // with a following fibre-only line kept ("100% Merino Wool" / "Superwash") and junk
    // (OEKO-TEX, barcode digits, logo misreads) dropped.
    private fun extractMaterialComposition(lines: List<OcrLine>): String? {
        val pct = Regex("""\d{1,3}\s*%""")
        // Groups of consecutive "NN%" lines from the same text block. Labels often print
        // the composition twice (icon list + front); OCR may garble one copy ("21% ACryie
        // 9% Wool"), so a group whose percentages add up to 100 wins.
        val groups = mutableListOf<MutableList<String>>()
        var lastBlock = Int.MIN_VALUE
        var lastIndex = -2
        for ((i, line) in lines.withIndex()) {
            if (!pct.containsMatchIn(line.text)) continue
            var part = cleanComposition(line.text) ?: continue
            val next = lines.getOrNull(i + 1)?.text
            if (next != null && !pct.containsMatchIn(next)) {
                val ws = words(next)
                if (ws.isNotEmpty() && ws.all { it in compositionWords }) part += " " + next.trim()
            }
            val sameGroup = i == lastIndex + 1 && (line.block == lastBlock || line.block < 0)
            if (sameGroup) groups.last() += part else groups += mutableListOf(part)
            lastBlock = line.block; lastIndex = i
        }
        if (groups.isEmpty()) return null
        fun total(g: List<String>) = g.sumOf { p -> Regex("""(\d{1,3})%""").findAll(p).sumOf { it.groupValues[1].toInt() } }
        val chosen = groups.firstOrNull { total(it) in 99..101 }
            ?: groups.flatten().fold(mutableListOf<String>()) { acc, p -> if (acc.none { key(it) == key(p) }) acc += p; acc }.take(3)
        // "70% Polyamide | 21% Acrylic" / "21% Acrylic - 9% Wool" → "70% Polyamide, 21% Acrylic, 9% Wool"
        return chosen.joinToString(", ")
            .replace(Regex("""(\p{L})\s*[-|]?\s+(\d{1,3}%)"""), "$1, $2")
            .replace(Regex("""\s*,\s*,\s*"""), ", ")
    }

    private fun cleanComposition(raw: String): String? {
        val s = raw.replace(Regex("""(\d)\s*%\s*"""), "$1% ")
        // Keep from the first "NN%" on; drop words that aren't fibres or percentages
        val start = Regex("""\d{1,3}% """).find(s)?.range?.first ?: return null
        val kept = s.substring(start).split(Regex("\\s+")).filter { w ->
            val n = normalizeWord(w)
            w.matches(Regex("""\d{1,3}%""")) || w == "/" || w == "-" ||
                (n.isNotEmpty() && n.none { it.isDigit() } && n !in setOf("oeko", "tex", "standard") &&
                    (n in compositionWords || n.length >= 3 && n.all { it.isLetter() } && !isFieldWord(n)))
        }
        val out = kept.joinToString(" ").replace(Regex("""\s*/\s*$"""), "").trim()
        return out.takeIf { it.length > 5 && Regex("""\d{1,3}% \p{L}""").containsMatchIn(it) }
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

    // Ball weight in grams — also converts oz to g. Takes the first plausible ball weight:
    // OCR glues neighbouring digits on ("2154g"), so numbers out of range are skipped.
    private val commonBallWeights = setOf(20, 25, 40, 50, 75, 80, 100, 125, 150, 200, 250, 300, 400, 500)

    private fun extractBallWeightG(text: String): String? {
        val grams = Regex("""(?i)(?<![\d.,])(\d{2,4})\s*(?:g|gr|grs|grams?|gramos|gramas)(?![\p{L}])""")
            .findAll(text).mapNotNull { it.groupValues[1].toIntOrNull() }.toList()
        (grams.firstOrNull { it in commonBallWeights } ?: grams.firstOrNull { it in 10..500 })
            ?.let { return it.toString() }

        val ozPattern = Regex("""(?i)(\d+[.,]?\d*)\s*oz(?![\p{L}])""")
        return ozPattern.find(text)?.groupValues?.get(1)?.replace(',', '.')?.toFloatOrNull()
            ?.let { (it * 28.3495).toInt() }?.takeIf { it in 10..500 }?.toString()
    }

    // Ball length in meters — also converts yards. "165m." and "175 m / 191 yds" both work;
    // "4 mm" and "10 cm" never match.
    private fun extractBallLengthM(text: String): String? {
        val meters = Regex("""(?i)(?<![\d.,])(\d{2,4})\s*(?:m|mt|mts|metres?|meters?|metros?)(?![\p{L}])""")
            .findAll(text).mapNotNull { it.groupValues[1].toIntOrNull() }.firstOrNull { it in 20..3000 }
        if (meters != null) return meters.toString()

        val yards = Regex("""(?i)(?<![\d.,])(\d{2,4})\s*(?:yds?|yards?)(?![\p{L}])""")
            .findAll(text).mapNotNull { it.groupValues[1].toIntOrNull() }.firstOrNull { it in 20..3000 }
        return yards?.let { (it * 0.9144).toInt().toString() }
    }

    private val sizeValue = """(\d{1,2}(?:[.,]\d{1,2})?(?:\s*[-–]\s*\d{1,2}(?:[.,]\d{1,2})?)?)"""

    private fun cleanSize(raw: String): String =
        raw.replace(',', '.').replace(Regex("""\s*[-–]\s*"""), "-").trim()

    // Hook size in mm (crochet hook) — handles European decimal comma, captions on the line
    // above the value ("Crochet Needle Size\n3,5-4 mm") and "G/4mm" US notation.
    private fun extractHookSize(text: String): String? {
        val hookPattern = Regex(
            """(?is)(?:crochet\s+(?:(?:hook|needle)s?(?:\s+size)?|size)|\bhook(?:\s+size)?|ganchillo|crochê|gancho|griffe|haken)\b[^\d\n]{0,12}\n?[^\d\n]{0,12}$sizeValue\s*mm"""
        )
        hookPattern.find(text)?.groupValues?.get(1)?.let { return cleanSize(it) }

        val slashPattern = Regex("""\b[A-Z\d]{1,3}/(\d+[.,]?\d*)\s*mm""")
        slashPattern.find(text)?.groupValues?.get(1)?.let { return cleanSize(it) }
        return null
    }

    // Needle size in mm (knitting needles). Also "Knitting Needle Size\n4-5 mm" and, last,
    // a line that is only a size range ("3-4" in the crossed-needles box).
    private fun extractNeedleSize(text: String, lines: List<OcrLine>): String? {
        val pattern = Regex(
            """(?is)(?<!crochet\s)(?:knitting\s+)?(?:needle[s]?|agujas?|agulha[s]?|aiguilles?|nadeln?)(?:\s+size)?\b[^\d\n]{0,12}\n?[^\d\n]{0,12}$sizeValue\s*mm"""
        )
        pattern.findAll(text).firstOrNull { m ->
            // "Crochet Needle Size" is the hook, not the knitting needles
            !text.substring(maxOf(0, m.range.first - 9), m.range.first).lowercase().contains("crochet")
        }?.groupValues?.get(1)?.let { v ->
            val c = cleanSize(v)
            if (sizeInRange(c)) return c
            unglueRange(c)?.let { return it }
        }

        val rangeOnly = Regex("""^\s*(\d{1,2}(?:[.,]\d)?)\s*[-–]\s*(\d{1,2}(?:[.,]\d)?)\s*(?:mm)?\s*$""")
        val hookCaption = Regex("""(?i)crochet|hook|ganchillo|crochê|gancho|haken""")
        return lines.withIndex().firstNotNullOfOrNull { (i, l) ->
            // A range under a crochet caption is the hook ("Crochet Size" / "5-5,5 mm")
            if (i > 0 && hookCaption.containsMatchIn(lines[i - 1].text)) return@firstNotNullOfOrNull null
            rangeOnly.find(l.text)?.let { m ->
                val a = m.groupValues[1].replace(',', '.').toFloatOrNull()
                val b = m.groupValues[2].replace(',', '.').toFloatOrNull()
                if (a != null && b != null && a in 1.5f..15f && b in 1.5f..15f && a < b && b - a <= 3f)
                    cleanSize(m.groupValues[1] + "-" + m.groupValues[2]) else null
            }
        }
    }

    // OCR often drops the dash of a size range: "4-4,5 mm" read as "44,5 mm". A first digit
    // that is smaller than or equal to the rest, both sizes in range, is split back.
    private fun unglueRange(s: String): String? {
        val m = Regex("""^(\d)(\d(?:\.\d{1,2})?)$""").find(s) ?: return null
        val a = m.groupValues[1].toFloat(); val b = m.groupValues[2].toFloat()
        return if (a in 1.5f..15f && b in 1.5f..15f && a < b && b - a <= 3f) "${m.groupValues[1]}-${m.groupValues[2]}" else null
    }

    private fun sizeInRange(s: String): Boolean =
        s.split('-').all { it.toFloatOrNull()?.let { v -> v in 0.5f..25f } == true }

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
