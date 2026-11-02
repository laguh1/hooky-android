package com.hooky.app.util

import com.hooky.app.domain.model.YarnColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YarnColorTest {

    @Test
    fun hexRoundTrip() {
        assertEquals("#9CAF88", YarnColor.toHex(0x9CAF88))
        assertEquals(0x9CAF88, YarnColor.parseHex("#9CAF88"))
        assertNull(YarnColor.parseHex("green"))
        assertNull(YarnColor.parseHex(null))
    }

    @Test
    fun namesInThreeLanguagesAndDetectorNames() {
        val green = YarnColor.colourForName("Verde")
        assertEquals(green, YarnColor.colourForName("green"))
        assertEquals(YarnColor.colourForName("Sage"), YarnColor.colourForName("verde sálvia"))
        assertEquals(YarnColor.colourForName("azul marinho"), YarnColor.colourForName("Navy"))
        assertEquals(YarnColor.colourForName("lilás"), YarnColor.colourForName("Lilac"))
        assertNull(YarnColor.colourForName("Unknown"))
        assertNull(YarnColor.colourForName(""))
    }

    @Test
    fun lightAndDarkModifiers() {
        val green = YarnColor.colourForName("verde")!!
        assertNotEquals(green, YarnColor.colourForName("verde claro"))
        assertNotEquals(green, YarnColor.colourForName("verde escuro"))
        // "light" that is part of the name isn't applied twice
        assertEquals(YarnColor.colourForName("Light Grey"), YarnColor.colourForName("cinza claro"))
    }

    @Test
    fun savedColourWinsOverName() {
        assertEquals(0x123456, YarnColor.effective("#123456", "verde"))
        assertEquals(YarnColor.colourForName("verde"), YarnColor.effective(null, "verde"))
    }
}
