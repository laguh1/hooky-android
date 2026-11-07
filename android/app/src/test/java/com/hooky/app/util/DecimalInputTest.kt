package com.hooky.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecimalInputTest {

    private fun assertParses(expected: Float, input: String) =
        assertEquals("input \"$input\"", expected, input.toDecimalOrNull()!!, 0.0001f)

    @Test
    fun acceptsDotAndComma() {
        assertParses(12.5f, "12.50")
        assertParses(12.5f, "12,50")
        assertParses(3f, "3")
        assertParses(0.75f, ",75")
    }

    @Test
    fun ignoresSpaces() {
        assertParses(12.5f, " 12,5 ")
    }

    @Test
    fun handlesThousandsSeparators() {
        assertParses(1234.56f, "1.234,56")
        assertParses(1234.56f, "1,234.56")
    }

    @Test
    fun rejectsJunk() {
        assertNull("".toDecimalOrNull())
        assertNull("abc".toDecimalOrNull())
        assertNull("1,2,3".toDecimalOrNull())
    }
}

class TextCleanTest {
    @Test
    fun joinsScannedLines() {
        assertEquals("100% Merino Wool Superwash Lndeobky", "100% Merino Wool\nSuperwash\r\n Lndeobky ".oneLine())
        assertEquals("Shine", "Shine".oneLine())
    }
}

class InputTextTest {
    private val pt = java.util.Locale.forLanguageTag("pt-BR")

    @Test
    fun localeSeparatorAndNoTrailingZeros() {
        assertEquals("25", 25.0f.toInputText(pt))
        assertEquals("9,5", 9.5f.toInputText(pt))
        assertEquals("120,25", 120.25f.toInputText(pt))
        assertEquals("1234,5", 1234.5f.toInputText(pt))
        assertEquals("9.5", 9.5f.toInputText(java.util.Locale.US))
    }

    @Test
    fun roundTrips() {
        listOf(0.5f, 9.5f, 120.25f, 1234.56f).forEach { v ->
            assertEquals(v, v.toInputText(pt).toDecimalOrNull()!!, 0.001f)
        }
    }
}
