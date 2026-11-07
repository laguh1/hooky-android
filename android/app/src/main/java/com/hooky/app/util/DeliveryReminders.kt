package com.hooky.app.util

import java.time.LocalDate

/**
 * Which delivery reminders to send today (1.11). Two per delivery:
 *  - SOON: once, when the delivery is 1–[DueDate.SOON_DAYS] days away
 *  - TODAY: once, on the day (or the day after, if the phone was off that day)
 * [alreadySent] holds keys of reminders sent before, so a reminder never repeats; changing
 * the date makes a new key, so a moved delivery is reminded again.
 */
object DeliveryReminders {
    enum class Kind { SOON, TODAY }

    data class Delivery(val id: Int, val name: String, val dueDate: String?, val destination: String, val archived: Boolean)

    data class Reminder(val id: Int, val name: String, val kind: Kind, val daysLeft: Long, val key: String)

    fun key(id: Int, dueDate: String, kind: Kind) = "$id|$dueDate|$kind"

    fun due(deliveries: List<Delivery>, today: LocalDate, alreadySent: Set<String>): List<Reminder> =
        deliveries.mapNotNull { d ->
            if (d.archived || !DueDate.isOpen(d.dueDate, d.destination)) return@mapNotNull null
            val date = d.dueDate!!
            val days = DueDate.daysLeft(date, today) ?: return@mapNotNull null
            val kind = when (days) {
                in 1..DueDate.SOON_DAYS.toLong() -> Kind.SOON
                0L, -1L -> Kind.TODAY
                else -> return@mapNotNull null
            }
            val key = key(d.id, date, kind)
            if (key in alreadySent) null else Reminder(d.id, d.name, kind, days, key)
        }

    /** Keys worth keeping: drop those for dates more than a week ago, so the set stays small. */
    fun prune(sent: Set<String>, today: LocalDate): Set<String> =
        sent.filterTo(mutableSetOf()) { k ->
            val date = k.split('|').getOrNull(1)?.let { DueDate.parse(it) } ?: return@filterTo false
            !date.isBefore(today.minusDays(7))
        }
}
