package com.hooky.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DueDateTest {
    private val today = LocalDate.of(2026, 12, 10)

    @Test
    fun daysLeft() {
        assertEquals(0L, DueDate.daysLeft("2026-12-10", today))
        assertEquals(5L, DueDate.daysLeft("2026-12-15", today))
        assertEquals(-2L, DueDate.daysLeft("2026-12-08", today))
        assertNull(DueDate.daysLeft(null, today))
        assertNull(DueDate.daysLeft("", today))
        assertNull(DueDate.daysLeft("15/12/2026", today))
    }

    @Test
    fun deliveredPiecesAreNotOpen() {
        assertTrue(DueDate.isOpen("2026-12-15", "FOR_SALE"))
        assertTrue(DueDate.isOpen("2026-12-15", "FOR_GIFT"))
        assertFalse(DueDate.isOpen("2026-12-15", "SOLD"))
        assertFalse(DueDate.isOpen("2026-12-15", "GIFTED"))
        assertFalse(DueDate.isOpen(null, "FOR_SALE"))
    }

    @Test
    fun soonOrLate() {
        assertTrue(DueDate.isSoonOrLate("2026-12-13", "FOR_SALE", today))   // 3 days
        assertFalse(DueDate.isSoonOrLate("2026-12-14", "FOR_SALE", today))  // 4 days
        assertTrue(DueDate.isSoonOrLate("2026-12-01", "FOR_SALE", today))   // late
        assertFalse(DueDate.isSoonOrLate("2026-12-01", "SOLD", today))      // delivered
    }
}
