package com.hooky.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkSession(
    val date: String,
    val durationMinutes: Int,
    val notes: String? = null,
    val rowsCompleted: Int? = null
)
