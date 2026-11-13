package com.hooky.app.data.scanner

import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * OCR readings modelled on the five real labels scanned on the S22 on 2026-11-13
 * (LindeHobby Merino Bliss 175, Carícia Frescor, Utopia Crafts Perfect DK, DROPS Brushed
 * Alpaca Silk). Each line is (text, height px, block); the misreads are the ones ML Kit made.
 */
class YarnLabelParserTest {

    private fun scan(vararg lines: Triple<String, Int, Int>) =
        YarnLabelParser.parse(lines.joinToString("\n") { it.first }, lines.map { OcrLine(it.first, it.second, it.third) })

    private fun l(text: String, h: Int, block: Int) = Triple(text, h, block)

    @Test
    fun lindeHobby_front_scriptLogoIsNotTheName() {
        val r = scan(
            l("Lndeobiy", 120, 0),
            l("Merino Bliss 175", 90, 1),
            l("100% Merino Wool", 30, 2),
            l("Superwash", 30, 2),
            l("OEKO-TEX", 20, 3),
            l("LindeHobby", 18, 4),
            l("Colour", 20, 5),
            l("8 435496 512345", 25, 6),
            l("175m", 22, 7),
        )
        assertEquals("Merino Bliss 175", r.name)
        assertEquals("LindeHobby", r.brand)
        assertEquals("100% Merino Wool Superwash", r.materialComposition)
        assertEquals(Material.WOOL, r.material)
        assertEquals("175", r.ballLengthM)
        assertNull(r.colorCode) // barcode on the line under "Colour" is not a colour code
    }

    @Test
    fun lindeHobby_back_iconCaptionsAreNotTheName() {
        val r = scan(
            l("Lndefobby", 80, 0),
            l("Merino Bliss 175", 60, 1),
            l("Crochet Needle Size", 20, 2),
            l("3,5-4 mm", 22, 2),
            l("Knitting Needle Size", 20, 3),
            l("4-5 mm", 22, 3),
            l("Yarn Weight", 20, 4),
            l("2154g", 22, 4),
            l("50g", 22, 4),
            l("Yarn Length", 20, 5),
            l("175m", 22, 5),
        )
        assertEquals("Merino Bliss 175", r.name)
        assertEquals("LindeHobby", r.brand)
        assertEquals("3.5", r.hookSizeMm) // smaller size: the yarn stores one number
        assertEquals("4-5", r.needleSizeMm)
        assertEquals("50", r.ballWeightG)
        assertEquals("175", r.ballLengthM)
    }

    @Test
    fun gluedDigitsAreNotABallWeight() {
        assertNull(scan(l("Yarn Weight", 20, 0), l("2154g", 22, 0)).ballWeightG)
    }

    @Test
    fun caricia_front_brandIsCorrected() {
        val r = scan(l("CARICIi", 100, 0), l("Frescor", 70, 1))
        assertEquals("Carícia", r.brand)
        assertEquals("Frescor", r.name)
    }

    @Test
    fun caricia_back_trailingDotsAndColCode() {
        val r = scan(
            l("100% Acrílico / Acrylic", 30, 0),
            l("75g. 165m.", 30, 1),
            l("3-4", 25, 2),
            l("COL 122", 25, 3),
            l("LOT 24F08", 25, 3),
        )
        assertEquals(Material.ACRYLIC, r.material)
        assertEquals("75", r.ballWeightG)
        assertEquals("165", r.ballLengthM)
        assertEquals("3-4", r.needleSizeMm)
        assertEquals("122", r.colorCode)
    }

    @Test
    fun utopia_front_nameWrapsAndBrandFromAlias() {
        val r = scan(
            l("PERFECT", 110, 0),
            l("DK", 100, 0),
            l("UTOPIA", 40, 1),
            l("CRAFTS", 40, 1),
            l("NITTING", 35, 2),
            l("100% Acrylic", 30, 3),
        )
        assertEquals("Utopia Crafts", r.brand)
        assertEquals("Perfect DK", r.name)
        assertEquals(Material.ACRYLIC, r.material)
        assertEquals(WeightCategory.DK, r.weightCategory)
    }

    @Test
    fun utopia_back_alphanumericColourCodeAndCleanComposition() {
        val r = scan(
            l("Colour: UC1000  Lot 1210", 22, 0),
            l("100%Acrylic 6 40 OEKO", 22, 1),
            l("100g", 22, 2),
            l("300m", 22, 2),
        )
        assertEquals("UC1000", r.colorCode)
        assertEquals("100% Acrylic", r.materialComposition)
        assertEquals("100", r.ballWeightG)
        assertEquals("300", r.ballLengthM)
    }

    @Test
    fun drops_sloganIsNotTheColour_nameJoinsTwoLines() {
        val r = scan(
            l("DROPS", 80, 0),
            l("Bruahed", 90, 1),
            l("Alpaca Silk", 85, 1),
            l("colour", 20, 2),
            l("A luxurious blend of brushed", 20, 3),
            l("77% Alpaca, 23% Silk", 20, 4),
            l("25 g = approx 140 m", 20, 5),
        )
        assertEquals("DROPS", r.brand)
        assertEquals("Bruahed Alpaca Silk", r.name)
        assertNull(r.colorName)
        assertEquals("140", r.ballLengthM)
        assertEquals("25", r.ballWeightG)
        assertEquals(Material.BLEND, r.material)
    }

    @Test
    fun colourNameOnTheSameLine() {
        val r = scan(l("Katia", 60, 0), l("Merino Aran", 50, 1), l("Color: Rojo 52", 20, 2))
        assertEquals("Rojo", r.colorName)
        assertEquals("52", r.colorCode)
        assertEquals("Merino Aran", r.name)
    }

    @Test
    fun hookSizeNotTakenAsLength() {
        val r = scan(l("Hook 4 mm", 20, 0), l("10 cm", 20, 0))
        assertEquals("4", r.hookSizeMm)
        assertNull(r.ballLengthM)
    }

    @Test
    fun rotatedPassFillsOnlyNumbers() {
        val primary = scan(l("CARICIi", 100, 0), l("Frescor", 70, 1))
        val rotated = scan(l("ZZl 7OD", 120, 0), l("COL 122", 25, 1))
        val merged = YarnLabelParser.fillFromRotated(primary, rotated)
        assertEquals("Frescor", merged.name)
        assertEquals("Carícia", merged.brand)
        assertEquals("122", merged.colorCode)
    }

    // LindeHobby Shine, scanned on the S22 2026-11-13 with the 1.12 parser: colour code and
    // hook size were missing ("Colour:" above the code; "Crochet Size", not "Crochet Hook").
    @Test
    fun lindeHobbyShine_captionAboveCode_crochetSize_needleGaugeIgnored() {
        val r = scan(
            l("Composition", 20, 0),
            l("70% Polyamide", 18, 0),
            l("21% Acrylic - 9% Wool", 18, 0),
            l("Needle Gauge", 20, 1),
            l("20 sts / 25 rows", 18, 1),
            l("10x10 cm", 18, 1),
            l("Knitting Needle Size", 20, 2),
            l("4-4,5 mm", 18, 2),
            l("Crochet Gauge", 20, 3),
            l("15 sts / 18 rows", 18, 3),
            l("Crochet Size", 20, 4),
            l("5-5,5 mm", 18, 4),
            l("Yarn Weight", 20, 5),
            l("50g - 1,76oz", 18, 5),
            l("Yarn Lenght", 20, 6),
            l("115m - 125yd", 18, 6),
            l("Shine", 90, 7),
            l("70% Polyamide | 21% Acrylic", 25, 8),
            l("9% Wool", 25, 8),
            l("5 714215 064256", 25, 9),
            l("Colour: Dyelot", 18, 10),
            l("LH-SHI-01 1025", 30, 10),
            l("LindeHobby", 30, 11),
        )
        assertEquals("Shine", r.name)
        assertEquals("LindeHobby", r.brand)
        assertEquals("LH-SHI-01", r.colorCode)
        assertEquals("5", r.hookSizeMm)
        assertEquals("4-4.5", r.needleSizeMm)
        assertEquals("70% Polyamide, 21% Acrylic, 9% Wool", r.materialComposition)
        assertEquals("50", r.ballWeightG)
        assertEquals("115", r.ballLengthM)
        assertEquals(Material.BLEND, r.material)
    }

    @Test
    fun colourCaptionInItsOwnBlock() {
        val r = scan(l("Colour:", 18, 0), l("LH-SHI-01", 30, 0), l("Dyelot", 18, 1), l("1025", 30, 1))
        assertEquals("LH-SHI-01", r.colorCode)
    }

    // The exact ML Kit lines from the S22 (debug log, 2026-11-13 15:32): two photos of the
    // LindeHobby Shine label. Dash dropped in "44,5 mm", "Dyelot" read between "Colour:" and
    // the code, composition printed twice (one copy garbled).
    @Test
    fun lindeHobbyShine_realOcr_iconSide() {
        val r = scan(
            l("X", 123, 0), l("Polyarmide", 56, 1), l("21% ACryie 9% Wool", 105, 2), l("Needle Gauge", 100, 2),
            l("20 sts/25 rows", 91, 3), l("10x10 Cm", 70, 3), l("Knitting Needle Size", 100, 4), l("44,5 mm", 85, 4),
            l("Crochet Gauge", 108, 4), l("15 sts / 18 rows", 93, 4), l("10x10 cm", 78, 4), l("Crochet Size", 82, 4),
            l("5-5,5 mm", 91, 4), l("Yarn Weight", 111, 4), l("50g- 1,7602", 75, 4), l("Yarn Lenght", 100, 4),
            l("115m - 125yd", 85, 4), l("Shine", 269, 5), l("70% Polyamide | 21% Acrylic", 156, 6), l("9% Wool", 103, 6),
            l("LndeHebby", 336, 7),
        )
        assertEquals("Shine", r.name)
        assertEquals("LindeHobby", r.brand)
        assertEquals("4-4.5", r.needleSizeMm)
        assertEquals("5", r.hookSizeMm)
        assertEquals("70% Polyamide, 21% Acrylic, 9% Wool", r.materialComposition)
        assertEquals("50", r.ballWeightG)
        assertEquals("115", r.ballLengthM)
    }

    @Test
    fun lindeHobbyShine_realOcr_barcodeSide() {
        val r = scan(
            l("S55 Mm", 70, 0), l("Yarn Weight", 77, 0), l("Sog- L.7607", 61, 0), l("Yarn Lenght", 81, 0),
            l("115h-125yd", 79, 0), l("Shine", 140, 1), l("70% Polyamide | 21% Acrylic", 92, 2), l("9% Wool", 60, 2),
            l("LndeCobby", 200, 3), l("5 714215 064256", 69, 4), l("Colour:", 49, 5), l("Dyelot", 65, 6),
            l("LH-SHI-01 1025", 108, 7), l("OEKO", 77, 8), l("TEX", 88, 8), l("STANDARD", 54, 9), l("100", 46, 9),
            l("21.HTR.94797", 45, 10), l("HOHENSTEIN HTTI", 48, 10), l("LindeHobby", 118, 11),
            l("Explore your locai Lindekobby", 67, 12), l("oniine store for detaied", 47, 12),
            l("intormaion and froe patterns", 67, 13), l("Made in Turkey", 296, 14),
        )
        assertEquals("Shine", r.name)
        assertEquals("LindeHobby", r.brand)
        assertEquals("LH-SHI-01", r.colorCode)
    }
}
