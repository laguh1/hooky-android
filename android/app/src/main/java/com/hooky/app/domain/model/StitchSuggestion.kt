package com.hooky.app.domain.model

import com.hooky.app.domain.model.enums.Difficulty
import com.hooky.app.domain.model.enums.StitchCategory

data class StitchSuggestion(
    val id: Int,
    val name: String,
    val creator: String,
    val youtubeUrl: String,
    val category: StitchCategory? = null,
    val difficulty: Difficulty? = null,
    val description: String? = null
)
