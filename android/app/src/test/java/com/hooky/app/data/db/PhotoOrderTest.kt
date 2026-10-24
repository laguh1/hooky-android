package com.hooky.app.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoOrderTest {

    @Test
    fun reversesList() {
        assertEquals("[\"c\",\"b\",\"a\"]", reversePhotosJson("[\"a\",\"b\",\"c\"]"))
    }

    @Test
    fun leavesEmptySingleAndInvalidUntouched() {
        assertEquals("[]", reversePhotosJson("[]"))
        assertEquals("[\"a\"]", reversePhotosJson("[\"a\"]"))
        assertEquals("not json", reversePhotosJson("not json"))
    }
}
