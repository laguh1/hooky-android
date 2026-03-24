package com.crochet.manager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkSession(
    val date: String,
    val durationMinutes: Int,
    val notes: String? = null
)
