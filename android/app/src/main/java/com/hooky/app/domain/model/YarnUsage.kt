package com.hooky.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class YarnUsage(
    val yarnId: String,
    val balls: Float = 1f
)
