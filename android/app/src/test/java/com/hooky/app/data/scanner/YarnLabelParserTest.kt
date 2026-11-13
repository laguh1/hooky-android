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
        assertEquals("3.5-4", r.hookSizeMm)
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
}
