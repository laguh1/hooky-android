package com.hooky.app.domain.model

import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory

data class YarnLabelScanResult(
    val name: String? = null,
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
    val gauge: String? = null,
    val washTemp: String? = null,
    val machineWash: Boolean? = null,
    val handWash: Boolean? = null,
    val tumbleDry: Boolean? = null
) {
    val hasAnyData: Boolean get() = listOfNotNull(
        name, brand, colorName, colorCode, materialComposition,
        ballWeightG, ballLengthM, hookSizeMm, needleSizeMm, gauge, washTemp
    ).isNotEmpty() || material != null || weightCategory != null ||
        machineWash != null || handWash != null || tumbleDry != null

    // Merge two scan results: this takes priority, other fills in nulls
    fun mergeWith(other: YarnLabelScanResult): YarnLabelScanResult = YarnLabelScanResult(
        name = this.name ?: other.name,
        brand = this.brand ?: other.brand,
        colorName = this.colorName ?: other.colorName,
        colorCode = this.colorCode ?: other.colorCode,
        material = this.material ?: other.material,
        materialComposition = this.materialComposition ?: other.materialComposition,
        weightCategory = this.weightCategory ?: other.weightCategory,
        ballWeightG = this.ballWeightG ?: other.ballWeightG,
        ballLengthM = this.ballLengthM ?: other.ballLengthM,
        hookSizeMm = this.hookSizeMm ?: other.hookSizeMm,
        needleSizeMm = this.needleSizeMm ?: other.needleSizeMm,
        gauge = this.gauge ?: other.gauge,
        washTemp = this.washTemp ?: other.washTemp,
        machineWash = this.machineWash ?: other.machineWash,
        handWash = this.handWash ?: other.handWash,
        tumbleDry = this.tumbleDry ?: other.tumbleDry
    )
}
