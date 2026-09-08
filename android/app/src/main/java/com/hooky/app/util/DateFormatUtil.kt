package com.hooky.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Dates are stored as ISO-8601 (yyyy-MM-dd) but always shown to the user as dd/MM/yyyy,
// regardless of app language.
fun String.toDisplayDate(): String {
    if (isBlank()) return this
    return try {
        LocalDate.parse(this, isoFormatter).format(displayFormatter)
    } catch (_: Exception) {
        this
    }
}
