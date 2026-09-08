package com.hooky.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "needles")
data class NeedleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val needleId: String,
    val name: String,
    val type: String,                   // NeedleType enum name
    val sizeMm: Float? = null,
    val sizeLabel: String? = null,      // e.g. "US H-8", "UK 6"
    val material: String? = null,       // e.g. "Aluminum", "Bamboo"
    val brand: String? = null,
    val quantity: Int? = 1,
    val photos: String = "[]",          // JSON: List<String>
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
