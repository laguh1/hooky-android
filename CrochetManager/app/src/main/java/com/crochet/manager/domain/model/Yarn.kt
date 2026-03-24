package com.crochet.manager.domain.model

import com.crochet.manager.domain.model.enums.Material
import com.crochet.manager.domain.model.enums.WeightCategory

data class Yarn(
    val id: Int = 0,
    val yarnId: String,
    val name: String,
    val brand: String? = null,
    val color: String,
    val colorCode: String? = null,
    val material: Material,
    val materialComposition: String? = null,
    val materialSpecs: String? = null,
    val weightCategory: WeightCategory? = null,
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
    val careInstructions: CareInstructions = CareInstructions(),
    val photos: List<String> = emptyList(),
    val notes: String? = null,
    val archived: Boolean = false,
    val archivedDate: String? = null,
    val archivedReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
