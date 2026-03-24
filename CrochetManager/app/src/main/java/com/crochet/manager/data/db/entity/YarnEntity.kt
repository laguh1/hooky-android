package com.crochet.manager.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "yarns")
data class YarnEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val yarnId: String,
    val name: String,
    val brand: String? = null,
    val color: String,
    val colorCode: String? = null,
    val material: String,
    val materialComposition: String? = null,
    val materialSpecs: String? = null,
    val weightCategory: String? = null,
    val ballWeightG: Float? = null,
    val ballLengthM: Float? = null,
    val pricePaid: Float? = null,
    val purchaseLocation: String? = null,
    val purchaseLink: String? = null,
    val purchaseDate: String? = null,
    val quantityOwned: Int? = null,
    val hookSizeMm: Float? = null,
    val needleSizeMm: String? = null,
    val gauge: String? = null,
    val careInstructions: String = "{}",  // JSON: CareInstructions object
    val photos: String = "[]",            // JSON: List<String>
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
