package com.hooky.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CareInstructions(
    val machineWash: Boolean = false,
    val handWash: Boolean = false,
    val dryClean: Boolean = false,
    val bleach: Boolean = false,
    val tumbleDry: Boolean = false,
    val ironTemperature: String? = null,
    val washTemperature: String? = null,
    val notes: String? = null
)
