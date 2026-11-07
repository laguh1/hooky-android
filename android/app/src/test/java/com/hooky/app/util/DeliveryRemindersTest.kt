package com.hooky.app.util

import com.hooky.app.util.DeliveryReminders.Delivery
import com.hooky.app.util.DeliveryReminders.Kind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DeliveryRemindersTest {
    private val today = LocalDate.of(2026, 12, 10)
    private fun d(id: Int, due: String?, dest: String = "FOR_SALE", archived: Boolean = false) =
        Delivery(id, "Piece $id", due, dest, archived)

    @Test
    fun soonAndToday() {
        val r = DeliveryReminders.due(
            listOf(d(1, "2026-12-13"), d(2, "2026-12-10"), d(3, "2026-12-20"), d(4, "2026-12-09"), d(5, "2026-12-01")),
            today, emptySet()
        )
        assertEquals(listOf(1 to Kind.SOON, 2 to Kind.TODAY, 4 to Kind.TODAY), r.map { it.id to it.kind })
    }

    @Test
    fun neverTwice() {
        val sent = setOf(DeliveryReminders.key(1, "2026-12-12", Kind.SOON))
        assertTrue(DeliveryReminders.due(listOf(d(1, "2026-12-12")), today, sent).isEmpty())
        // Moved to another date: reminded again
        assertEquals(1, DeliveryReminders.due(listOf(d(1, "2026-12-11")), today, sent).size)
    }

    @Test
    fun skipsDeliveredArchivedAndUndated() {
        val r = DeliveryReminders.due(
            listOf(d(1, "2026-12-11", "SOLD"), d(2, "2026-12-11", "GIFTED"), d(3, "2026-12-11", archived = true), d(4, null)),
            today, emptySet()
        )
        assertTrue(r.isEmpty())
    }

    @Test
    fun prunesOldKeys() {
        val kept = DeliveryReminders.prune(setOf("1|2026-12-01|TODAY", "2|2026-12-05|SOON", "bad"), today)
        assertEquals(setOf("2|2026-12-05|SOON"), kept)
    }
}
