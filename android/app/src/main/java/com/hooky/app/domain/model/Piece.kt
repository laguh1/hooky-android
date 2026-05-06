package com.hooky.app.domain.model

import com.hooky.app.domain.model.enums.Destination
import com.hooky.app.domain.model.enums.PieceType
import com.hooky.app.domain.model.enums.WorkStatus

data class Piece(
    val id: Int = 0,
    val pieceId: String,
    val name: String,
    val type: PieceType,
    val workStatus: WorkStatus,
    val destination: Destination,
    val widthCm: Float? = null,
    val lengthCm: Float? = null,
    val dateStarted: String? = null,
    val dateFinished: String? = null,
    val workHours: Float? = null,
    val workSessions: List<WorkSession> = emptyList(),
    val hookSizeMm: Float? = null,
    val photos: List<String> = emptyList(),
    val price: Float? = null,
    val materialCost: Float? = null,
    val giftRecipient: String? = null,
    val salePlatform: String? = null,
    val saleLink: String? = null,
    val soldDate: String? = null,
    val soldPrice: Float? = null,
    val yarnsUsed: List<String> = emptyList(),
    val stitchesUsed: List<String> = emptyList(),
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
