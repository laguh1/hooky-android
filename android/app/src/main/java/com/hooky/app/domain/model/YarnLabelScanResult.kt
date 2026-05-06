package com.hooky.app.domain.model

import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory

data class YarnLabelScanResult(
    val brand: String? = null,
    val colorName: String? = null,
    val colorCode: String? = null,
    val material: Material? = null,
    val materialComposition: String? = null,
    val weightCategory: WeightCategory? = null,
    val ballWeightG: String? = null,
    val ballLengthM: String? = null,
    val hookSizeMm: String? = null,
    val needleSizeMm: String? = null,
    val gauge: String? = null
) {
    val hasAnyData: Boolean get() = listOfNotNull(
        brand, colorName, colorCode, materialComposition,
        ballWeightG, ballLengthM, hookSizeMm, needleSizeMm, gauge
    ).isNotEmpty() || material != null || weightCategory != null
}
