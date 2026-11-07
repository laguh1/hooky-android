package com.hooky.app.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Delivery dates on pieces (1.11). A delivery is "open" while the piece has a due date and
 * hasn't been handed over yet: once it's Sold or Gifted, the date is history, not a deadline.
 */
object DueDate {
    /** Days before the due date when it counts as "soon" (badge turns purple, reminder fires). */
    const val SOON_DAYS = 3

    private val DELIVERED = setOf("SOLD", "GIFTED")

    fun parse(iso: String?): LocalDate? =
        iso?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    fun isOpen(dueDate: String?, destination: String): Boolean =
        parse(dueDate) != null && destination !in DELIVERED

    /** Days from [today] to the due date: 0 = today, negative = late, null = no date. */
    fun daysLeft(dueDate: String?, today: LocalDate = LocalDate.now()): Long? =
        parse(dueDate)?.let { ChronoUnit.DAYS.between(today, it) }

    fun isSoonOrLate(dueDate: String?, destination: String, today: LocalDate = LocalDate.now()): Boolean {
        if (!isOpen(dueDate, destination)) return false
        val days = daysLeft(dueDate, today) ?: return false
        return days <= SOON_DAYS
    }
}
