package com.crochet.manager.domain.model

import com.crochet.manager.domain.model.enums.Difficulty
import com.crochet.manager.domain.model.enums.StitchCategory

data class Stitch(
    val id: Int = 0,
    val stitchId: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val nameEs: String? = null,
    val abbreviation: String? = null,
    val category: StitchCategory? = null,
    val difficulty: Difficulty? = null,
    val description: String,
    val hookfullyLink: String? = null,
    val instructionLink: String? = null,
    val videoLink: String? = null,
    val photos: List<String> = emptyList(),
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
