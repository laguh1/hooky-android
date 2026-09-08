package com.hooky.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ideas")
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val source: String,
    val url: String,
    val description: String? = null,
    val locale: String,       // "en", "pt", "es"
    val isSeeded: Boolean = false,  // true = default content, false = user-added
    val sortOrder: Int = 0
)
