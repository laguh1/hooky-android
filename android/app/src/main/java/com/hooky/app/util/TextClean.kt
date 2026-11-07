package com.hooky.app.util

/**
 * Joins text that came in on several lines (e.g. from the label scanner) into one line.
 * The yarn form fields are single-line, so line breaks used to hide the rest of the text
 * where it could not be seen or deleted (bug found 2026-11-03).
 */
fun String.oneLine(): String = replace(Regex("\\s*[\\r\\n]+\\s*"), " ").trim()
