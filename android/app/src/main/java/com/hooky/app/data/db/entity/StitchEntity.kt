package com.hooky.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "stitches")
data class StitchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stitchId: String,
    val name: String,
    val nameAliases: String = "[]",  // JSON: List<String>
    val nameEs: String? = null,
    val abbreviation: String? = null,
    val category: String? = null,
    val difficulty: String? = null,
    val description: String,
    val hookfullyLink: String? = null,
    val instructionLink: String? = null,
    val videoLink: String? = null,
    val photos: String = "[]",       // JSON: List<String>
    val notes: String? = null,
    val chartPath: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
