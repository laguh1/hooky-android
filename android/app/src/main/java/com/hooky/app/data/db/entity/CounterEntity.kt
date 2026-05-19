package com.hooky.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "counters")
data class CounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pieceId: String,
    val name: String,
    val count: Int = 0,
    val target: Int? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
