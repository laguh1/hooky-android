package com.hooky.app.util

import com.hooky.app.domain.model.YarnUsage
import com.hooky.app.domain.model.parseYarnUsages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class YarnAmountFormatTest {

    @Test
    fun formatsWholeAndCommonFractions() {
        assertEquals("2", formatBalls(2f))
        assertEquals("2⅓", formatBalls(2f + 1f / 3))
        assertEquals("½", formatBalls(0.5f))
        assertEquals("1¾", formatBalls(1.75f))
    }

    @Test
    fun formatsOtherDecimalsInLocale() {
        val default = Locale.getDefault()
        try {
            Locale.setDefault(Locale("pt", "BR"))
            assertEquals("2,4", formatBalls(2.4f))
            Locale.setDefault(Locale.US)
            assertEquals("2.4", formatBalls(2.4f))
        } finally {
            Locale.setDefault(default)
        }
    }

    @Test
    fun parsesDecimalsFractionsAndGlyphs() {
        assertEquals(2.5f, parseBalls("2,5")!!, 0.001f)
        assertEquals(2.5f, parseBalls("2.5")!!, 0.001f)
        assertEquals(2f + 1f / 3, parseBalls("2 1/3")!!, 0.001f)
        assertEquals(1f / 3, parseBalls("1/3")!!, 0.001f)
        assertEquals(2f + 1f / 3, parseBalls("2⅓")!!, 0.001f)
        assertEquals(0.75f, parseBalls("¾")!!, 0.001f)
        // round trip
        assertEquals(2f + 2f / 3, parseBalls(formatBalls(2f + 2f / 3))!!, 0.001f)
    }

    @Test
    fun rejectsJunk() {
        assertNull(parseBalls(""))
        assertNull(parseBalls("abc"))
        assertNull(parseBalls("1/0"))
        assertNull(parseBalls("1 2 3"))
    }

    @Test
    fun readsOldAndNewYarnsUsedFormats() {
        assertEquals(listOf(YarnUsage("YARN-001", 1f)), parseYarnUsages("[\"YARN-001\"]"))
        assertEquals(listOf(YarnUsage("YARN-002", 2.5f)), parseYarnUsages("[{\"yarnId\":\"YARN-002\",\"balls\":2.5}]"))
        assertEquals(emptyList<YarnUsage>(), parseYarnUsages("not json"))
    }

    @Test
    fun approximateTotalsUseCraftFractions() {
        assertEquals("≈ 13¼", formatBallsApprox(13f + 1f / 6))
        assertEquals("12½", formatBallsApprox(12.5f))
        assertEquals("4", formatBallsApprox(4f))
        assertEquals("≈ 5", formatBallsApprox(4.95f))
    }
}
