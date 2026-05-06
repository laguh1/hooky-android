package com.hooky.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "id_counters")
data class IdCounterEntity(
    @PrimaryKey val entityType: String,
    val currentCount: Int = 0
)
