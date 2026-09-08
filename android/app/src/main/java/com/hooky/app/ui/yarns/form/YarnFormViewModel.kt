package com.hooky.app.ui.yarns.form

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.data.repository.YarnRepository
import com.hooky.app.data.scanner.PaletteColorExtractor
import com.hooky.app.data.scanner.ScanMode
import com.hooky.app.data.scanner.YarnLabelScannerService
import com.hooky.app.domain.model.CareInstructions
import com.hooky.app.domain.model.NeedleScanResult
import com.hooky.app.domain.model.YarnLabelScanResult
import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory
import com.hooky.app.ui.util.movePhotosToStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class YarnFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false,
    // Form fields
    val name: String = "",
    val brand: String = "",
    val color: String = "",
    val colorCode: String = "",
    val material: Material = Material.OTHER,
    val materialComposition: String = "",
    val materialSpecs: String = "",
    val weightCategory: WeightCategory? = null,
    val ballWeightG: String = "",
    val ballLengthM: String = "",
    val pricePaid: String = "",
    val purchaseLocation: String = "",
    val purchaseLink: String = "",
    val purchaseDate: String = "",
    val quantityOwned: String = "",
    val hookSizeMm: String = "",
    val needleSizeMm: String = "",
    val gauge: String = "",
    // Care instructions
    val machineWash: Boolean = false,
    val handWash: Boolean = false,
    val dryClean: Boolean = false,
    val bleach: Boolean = false,
    val tumbleDry: Boolean = false,
    val washTemperature: String = "",
    val careNotes: String = "",
    val photos: List<String> = emptyList(),
    val notes: String = "",
    val hasUnsavedChanges: Boolean = false,
    // Validation
    val nameError: String? = null,
    // Scanning
    val scanMode: ScanMode = ScanMode.NONE,
    val isScanning: Boolean = false,
    val scanResult: YarnLabelScanResult? = null,
    val accumulatedScanResult: YarnLabelScanResult? = null,
    val needleScanResult: NeedleScanResult? = null,
    // Color suggestion (auto-detected from photo via Palette API)
    val suggestedColor: String? = null,
    val suggestedColorRgb: Int? = null
)

sealed interface YarnFormAction {
    data class NameChanged(val value: String) : YarnFormAction
    data class BrandChanged(val value: String) : YarnFormAction
    data class ColorChanged(val value: String) : YarnFormAction
    data class ColorCodeChanged(val value: String) : YarnFormAction
    data class MaterialChanged(val value: Material) : YarnFormAction
    data class MaterialCompositionChanged(val value: String) : YarnFormAction
    data class MaterialSpecsChanged(val value: String) : YarnFormAction
    data class WeightCategoryChanged(val value: WeightCategory?) : YarnFormAction
    data class BallWeightGChanged(val value: String) : YarnFormAction
    data class BallLengthMChanged(val value: String) : YarnFormAction
    data class PricePaidChanged(val value: String) : YarnFormAction
    data class PurchaseLocationChanged(val value: String) : YarnFormAction
    data class PurchaseLinkChanged(val value: String) : YarnFormAction
    data class PurchaseDateChanged(val value: String) : YarnFormAction
    data class QuantityOwnedChanged(val value: String) : YarnFormAction
    data class HookSizeMmChanged(val value: String) : YarnFormAction
    data class NeedleSizeMmChanged(val value: String) : YarnFormAction
    data class GaugeChanged(val value: String) : YarnFormAction
    data class MachineWashChanged(val value: Boolean) : YarnFormAction
    data class HandWashChanged(val value: Boolean) : YarnFormAction
    data class DryCleanChanged(val value: Boolean) : YarnFormAction
    data class BleachChanged(val value: Boolean) : YarnFormAction
    data class TumbleDryChanged(val value: Boolean) : YarnFormAction
    data class WashTemperatureChanged(val value: String) : YarnFormAction
    data class CareNotesChanged(val value: String) : YarnFormAction
    data class PhotoAdded(val uri: String) : YarnFormAction
    data class PhotoReceived(val path: String) : YarnFormAction
    data class PhotoRemoved(val uri: String) : YarnFormAction
    data class PhotoReplaced(val oldPath: String, val newPath: String) : YarnFormAction
    data class NotesChanged(val value: String) : YarnFormAction
    object SaveYarn : YarnFormAction
    object ClearError : YarnFormAction
    // Color suggestion
    object ApplySuggestedColor : YarnFormAction
    object DismissSuggestedColor : YarnFormAction
    // Label scanning
    object ScanLabelRequested : YarnFormAction
    object ScanAnotherSide : YarnFormAction
    object ApplyScanResult : YarnFormAction
    object DismissScanResult : YarnFormAction
    // Needle/hook scanning
    data class NeedleScanRequested(val target: ScanMode) : YarnFormAction
    object ApplyNeedleScanResult : YarnFormAction
    object DismissNeedleScanResult : YarnFormAction
}

@HiltViewModel
class YarnFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val yarnRepository: YarnRepository,
    private val labelScannerService: YarnLabelScannerService,
    private val colorExtractor: PaletteColorExtractor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val yarnId: Int? = savedStateHandle["id"]

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(
        YarnFormUiState(isEditMode = yarnId != null)
    )
    val uiState: StateFlow<YarnFormUiState> = _uiState.asStateFlow()

    init {
        if (yarnId != null) {
            loadExistingYarn(yarnId)
        }
    }

    private fun loadExistingYarn(id: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val yarn = yarnRepository.getYarnById(id)
                    .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                    .first()

                if (yarn != null) {
                    val photos = try {
                        json.decodeFromString<List<String>>(yarn.photos)
                    } catch (_: Exception) { emptyList() }

                    val care = try {
                        json.decodeFromString<CareInstructions>(yarn.careInstructions)
                    } catch (_: Exception) { CareInstructions() }

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            name = yarn.name,
                            brand = yarn.brand ?: "",
                            color = yarn.color,
                            colorCode = yarn.colorCode ?: "",
                            material = try { Material.valueOf(yarn.material) } catch (_: Exception) { Material.OTHER },
                            materialComposition = yarn.materialComposition ?: "",
                            materialSpecs = yarn.materialSpecs ?: "",
                            weightCategory = yarn.weightCategory?.let {
                                try { WeightCategory.valueOf(it) } catch (_: Exception) { null }
                            },
                            ballWeightG = yarn.ballWeightG?.toString() ?: "",
                            ballLengthM = yarn.ballLengthM?.toString() ?: "",
                            pricePaid = yarn.pricePaid?.toString() ?: "",
                            purchaseLocation = yarn.purchaseLocation ?: "",
                            purchaseLink = yarn.purchaseLink ?: "",
                            purchaseDate = yarn.purchaseDate ?: "",
                            quantityOwned = yarn.quantityOwned?.toString() ?: "",
                            hookSizeMm = yarn.hookSizeMm?.toString() ?: "",
                            needleSizeMm = yarn.needleSizeMm ?: "",
                            gauge = yarn.gauge ?: "",
                            machineWash = care.machineWash,
                            handWash = care.handWash,
                            dryClean = care.dryClean,
                            bleach = care.bleach,
                            tumbleDry = care.tumbleDry,
                            washTemperature = care.washTemperature ?: care.ironTemperature ?: "",
                            careNotes = care.notes ?: "",
                            photos = photos,
                            notes = yarn.notes ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onAction(action: YarnFormAction) {
        if (action !is YarnFormAction.ClearError && action !is YarnFormAction.SaveYarn) {
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
        when (action) {
            is YarnFormAction.NameChanged ->
                _uiState.update { it.copy(name = action.value, nameError = null) }
            is YarnFormAction.BrandChanged ->
                _uiState.update { it.copy(brand = action.value) }
            is YarnFormAction.ColorChanged ->
                _uiState.update { it.copy(color = action.value) }
            is YarnFormAction.ColorCodeChanged ->
                _uiState.update { it.copy(colorCode = action.value) }
            is YarnFormAction.MaterialChanged ->
                _uiState.update { it.copy(material = action.value) }
            is YarnFormAction.MaterialCompositionChanged ->
                _uiState.update { it.copy(materialComposition = action.value) }
            is YarnFormAction.MaterialSpecsChanged ->
                _uiState.update { it.copy(materialSpecs = action.value) }
            is YarnFormAction.WeightCategoryChanged ->
                _uiState.update { it.copy(weightCategory = action.value) }
            is YarnFormAction.BallWeightGChanged ->
                _uiState.update { it.copy(ballWeightG = action.value) }
            is YarnFormAction.BallLengthMChanged ->
                _uiState.update { it.copy(ballLengthM = action.value) }
            is YarnFormAction.PricePaidChanged ->
                _uiState.update { it.copy(pricePaid = action.value) }
            is YarnFormAction.PurchaseLocationChanged ->
                _uiState.update { it.copy(purchaseLocation = action.value) }
            is YarnFormAction.PurchaseLinkChanged ->
                _uiState.update { it.copy(purchaseLink = action.value) }
            is YarnFormAction.PurchaseDateChanged ->
                _uiState.update { it.copy(purchaseDate = action.value) }
            is YarnFormAction.QuantityOwnedChanged ->
                _uiState.update { it.copy(quantityOwned = action.value) }
            is YarnFormAction.HookSizeMmChanged ->
                _uiState.update { it.copy(hookSizeMm = action.value) }
            is YarnFormAction.NeedleSizeMmChanged ->
                _uiState.update { it.copy(needleSizeMm = action.value) }
            is YarnFormAction.GaugeChanged ->
                _uiState.update { it.copy(gauge = action.value) }
            is YarnFormAction.MachineWashChanged ->
                _uiState.update { it.copy(machineWash = action.value) }
            is YarnFormAction.HandWashChanged ->
                _uiState.update { it.copy(handWash = action.value) }
            is YarnFormAction.DryCleanChanged ->
                _uiState.update { it.copy(dryClean = action.value) }
            is YarnFormAction.BleachChanged ->
                _uiState.update { it.copy(bleach = action.value) }
            is YarnFormAction.TumbleDryChanged ->
                _uiState.update { it.copy(tumbleDry = action.value) }
            is YarnFormAction.WashTemperatureChanged ->
                _uiState.update { it.copy(washTemperature = action.value) }
            is YarnFormAction.CareNotesChanged ->
                _uiState.update { it.copy(careNotes = action.value) }
            is YarnFormAction.PhotoAdded -> {
                _uiState.update { it.copy(photos = it.photos + action.uri) }
                if (_uiState.value.color.isBlank()) detectColor(action.uri)
            }
            is YarnFormAction.PhotoReceived -> {
                when (_uiState.value.scanMode) {
                    ScanMode.LABEL -> scanPhoto(action.path)
                    ScanMode.HOOK, ScanMode.NEEDLE -> scanNeedle(action.path, _uiState.value.scanMode)
                    ScanMode.NONE -> {
                        _uiState.update { it.copy(photos = it.photos + action.path) }
                        if (_uiState.value.color.isBlank()) detectColor(action.path)
                    }
                }
            }
            YarnFormAction.ApplySuggestedColor -> {
                val suggested = _uiState.value.suggestedColor ?: return
                _uiState.update { it.copy(color = suggested, suggestedColor = null, suggestedColorRgb = null) }
            }
            YarnFormAction.DismissSuggestedColor ->
                _uiState.update { it.copy(suggestedColor = null, suggestedColorRgb = null) }
            is YarnFormAction.PhotoRemoved ->
                _uiState.update { it.copy(photos = it.photos - action.uri) }
            is YarnFormAction.PhotoReplaced ->
                _uiState.update { state ->
                    state.copy(photos = state.photos.map { if (it == action.oldPath) action.newPath else it })
                }
            is YarnFormAction.NotesChanged ->
                _uiState.update { it.copy(notes = action.value) }
            YarnFormAction.SaveYarn -> saveYarn()
            YarnFormAction.ClearError ->
                _uiState.update { it.copy(error = null) }
            YarnFormAction.ScanLabelRequested ->
                _uiState.update { it.copy(scanMode = ScanMode.LABEL) }
            YarnFormAction.ScanAnotherSide ->
                _uiState.update { it.copy(accumulatedScanResult = it.scanResult, scanResult = null, scanMode = ScanMode.LABEL) }
            YarnFormAction.ApplyScanResult -> applyScanResult()
            YarnFormAction.DismissScanResult ->
                _uiState.update { it.copy(scanResult = null, accumulatedScanResult = null, scanMode = ScanMode.NONE) }
            is YarnFormAction.NeedleScanRequested ->
                _uiState.update { it.copy(scanMode = action.target) }
            YarnFormAction.ApplyNeedleScanResult -> applyNeedleScanResult()
            YarnFormAction.DismissNeedleScanResult ->
                _uiState.update { it.copy(needleScanResult = null, scanMode = ScanMode.NONE) }
        }
    }

    private fun detectColor(path: String) {
        viewModelScope.launch {
            val extracted = colorExtractor.extractFromPath(path) ?: return@launch
            // Only suggest if color field is still blank (user may have filled it manually)
            if (_uiState.value.color.isBlank()) {
                _uiState.update { it.copy(suggestedColor = extracted.name, suggestedColorRgb = extracted.rgb) }
            }
        }
    }

    private fun scanPhoto(path: String) {
        _uiState.update { it.copy(isScanning = true, scanMode = ScanMode.NONE) }
        viewModelScope.launch {
            try {
                val result = labelScannerService.scanFromPath(path)
                val accumulated = _uiState.value.accumulatedScanResult
                val merged = if (accumulated != null) result.mergeWith(accumulated) else result
                if (merged.hasAnyData) {
                    _uiState.update { it.copy(isScanning = false, scanResult = merged, accumulatedScanResult = null) }
                } else {
                    _uiState.update { it.copy(isScanning = false, accumulatedScanResult = null, error = "Couldn't read label info. Try a clearer, closer photo with good lighting.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, accumulatedScanResult = null, error = "Scan failed: ${e.message}") }
            }
        }
    }

    private fun scanNeedle(path: String, target: ScanMode) {
        _uiState.update { it.copy(isScanning = true, scanMode = ScanMode.NONE) }
        viewModelScope.launch {
            try {
                val result = labelScannerService.scanNeedleFromPath(path, target)
                if (result != null) {
                    _uiState.update { it.copy(isScanning = false, needleScanResult = result, scanMode = target) }
                } else {
                    _uiState.update { it.copy(isScanning = false, error = "Couldn't read size. Try a clearer, closer photo with good lighting.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = "Scan failed: ${e.message}") }
            }
        }
    }

    private fun applyNeedleScanResult() {
        val result = _uiState.value.needleScanResult ?: return
        val target = _uiState.value.scanMode
        _uiState.update { current ->
            when (target) {
                ScanMode.HOOK -> current.copy(hookSizeMm = result.sizeMm, needleScanResult = null, scanMode = ScanMode.NONE)
                ScanMode.NEEDLE -> current.copy(needleSizeMm = result.sizeMm, needleScanResult = null, scanMode = ScanMode.NONE)
                else -> current.copy(needleScanResult = null, scanMode = ScanMode.NONE)
            }
        }
    }

    private fun applyScanResult() {
        val result = _uiState.value.scanResult ?: return
        _uiState.update { current ->
            current.copy(
                scanResult = null,
                scanMode = ScanMode.NONE,
                name = result.name?.takeIf { it.isNotBlank() } ?: current.name,
                brand = result.brand?.takeIf { it.isNotBlank() } ?: current.brand,
                color = result.colorName?.takeIf { it.isNotBlank() } ?: current.color,
                colorCode = result.colorCode?.takeIf { it.isNotBlank() } ?: current.colorCode,
                material = result.material ?: current.material,
                materialComposition = result.materialComposition?.takeIf { it.isNotBlank() } ?: current.materialComposition,
                weightCategory = result.weightCategory ?: current.weightCategory,
                ballWeightG = result.ballWeightG?.takeIf { it.isNotBlank() } ?: current.ballWeightG,
                ballLengthM = result.ballLengthM?.takeIf { it.isNotBlank() } ?: current.ballLengthM,
                hookSizeMm = result.hookSizeMm?.takeIf { it.isNotBlank() } ?: current.hookSizeMm,
                needleSizeMm = result.needleSizeMm?.takeIf { it.isNotBlank() } ?: current.needleSizeMm,
                gauge = result.gauge?.takeIf { it.isNotBlank() } ?: current.gauge,
                machineWash = result.machineWash ?: current.machineWash,
                handWash = result.handWash ?: current.handWash,
                tumbleDry = result.tumbleDry ?: current.tumbleDry
            )
        }
    }

    private fun saveYarn() {
        val current = _uiState.value

        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, nameError = null) }

        viewModelScope.launch {
            try {
                val stringListSerializer = kotlinx.serialization.builtins.ListSerializer(
                    kotlinx.serialization.serializer<String>()
                )
                val careInstructions = CareInstructions(
                    machineWash = current.machineWash,
                    handWash = current.handWash,
                    dryClean = current.dryClean,
                    bleach = current.bleach,
                    tumbleDry = current.tumbleDry,
                    washTemperature = current.washTemperature.ifBlank { null },
                    notes = current.careNotes.ifBlank { null }
                )
                val careJson = json.encodeToString(CareInstructions.serializer(), careInstructions)

                val now = System.currentTimeMillis()

                if (current.isEditMode && yarnId != null) {
                    val permanentPhotos = movePhotosToStorage(context, current.photos, "yarns", yarnId.toString())
                    val photosJson = json.encodeToString(stringListSerializer, permanentPhotos)
                    val existing = yarnRepository.getYarnById(yarnId).first()
                    if (existing != null) {
                        val updated = existing.copy(
                            name = current.name,
                            brand = current.brand.ifBlank { null },
                            color = current.color.ifBlank { "Unknown" },
                            colorCode = current.colorCode.ifBlank { null },
                            material = current.material.name,
                            materialComposition = current.materialComposition.ifBlank { null },
                            materialSpecs = current.materialSpecs.ifBlank { null },
                            weightCategory = current.weightCategory?.name,
                            ballWeightG = current.ballWeightG.toFloatOrNull(),
                            ballLengthM = current.ballLengthM.toFloatOrNull(),
                            pricePaid = current.pricePaid.toFloatOrNull(),
                            purchaseLocation = current.purchaseLocation.ifBlank { null },
                            purchaseLink = current.purchaseLink.ifBlank { null },
                            purchaseDate = current.purchaseDate.ifBlank { null },
                            quantityOwned = current.quantityOwned.toIntOrNull(),
                            hookSizeMm = current.hookSizeMm.toFloatOrNull(),
                            needleSizeMm = current.needleSizeMm.ifBlank { null },
                            gauge = current.gauge.ifBlank { null },
                            careInstructions = careJson,
                            photos = photosJson,
                            notes = current.notes.ifBlank { null },
                            updatedAt = now
                        )
                        yarnRepository.updateYarn(updated)
                    }
                } else {
                    val newYarnId = yarnRepository.generateNextYarnId()
                    val permanentPhotos = movePhotosToStorage(context, current.photos, "yarns", newYarnId.toString())
                    val photosJson = json.encodeToString(stringListSerializer, permanentPhotos)
                    val entity = YarnEntity(
                        yarnId = newYarnId,
                        name = current.name,
                        brand = current.brand.ifBlank { null },
                        color = current.color.ifBlank { "Unknown" },
                        colorCode = current.colorCode.ifBlank { null },
                        material = current.material.name,
                        materialComposition = current.materialComposition.ifBlank { null },
                        materialSpecs = current.materialSpecs.ifBlank { null },
                        weightCategory = current.weightCategory?.name,
                        ballWeightG = current.ballWeightG.toFloatOrNull(),
                        ballLengthM = current.ballLengthM.toFloatOrNull(),
                        pricePaid = current.pricePaid.toFloatOrNull(),
                        purchaseLocation = current.purchaseLocation.ifBlank { null },
                        purchaseLink = current.purchaseLink.ifBlank { null },
                        purchaseDate = current.purchaseDate.ifBlank { null },
                        quantityOwned = current.quantityOwned.toIntOrNull(),
                        hookSizeMm = current.hookSizeMm.toFloatOrNull(),
                        needleSizeMm = current.needleSizeMm.ifBlank { null },
                        gauge = current.gauge.ifBlank { null },
                        careInstructions = careJson,
                        photos = photosJson,
                        notes = current.notes.ifBlank { null },
                        createdAt = now,
                        updatedAt = now
                    )
                    yarnRepository.insertYarn(entity)
                }

                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
