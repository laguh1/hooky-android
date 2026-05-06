package com.hooky.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pieces")
data class PieceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pieceId: String,
    val name: String,
    val type: String,
    val workStatus: String,
    val destination: String,
    val widthCm: Float? = null,
    val lengthCm: Float? = null,
    val dateStarted: String? = null,
    val dateFinished: String? = null,
    val workHours: Float? = null,
    val workSessions: String = "[]",   // JSON: List<WorkSession>
    val hookSizeMm: Float? = null,
    val photos: String = "[]",         // JSON: List<String>
    val price: Float? = null,
    val materialCost: Float? = null,
    val giftRecipient: String? = null,
    val salePlatform: String? = null,
    val saleLink: String? = null,
    val soldDate: String? = null,
    val soldPrice: Float? = null,
    val yarnsUsed: String = "[]",      // JSON: List<String> (YARN-IDs)
    val stitchesUsed: String = "[]",   // JSON: List<String> (STITCH-IDs)
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val rowCount: Int = 0,
    val targetRowCount: Int? = null,
    val timerTotalSeconds: Long = 0,
    val timerIsRunning: Boolean = false,
    val timerSessionStartAt: Long? = null,
    val needlesUsed: String = "[]",      // JSON: List<String> (NEEDLE-IDs)
)
