package com.crochet.manager.ui.yarns.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crochet.manager.data.db.entity.YarnEntity
import com.crochet.manager.data.repository.YarnRepository
import com.crochet.manager.domain.model.CareInstructions
import com.crochet.manager.domain.model.enums.Material
import com.crochet.manager.domain.model.enums.WeightCategory
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
    val ironTemperature: String = "",
    val careNotes: String = "",
    val photos: List<String> = emptyList(),
    val notes: String = "",
    // Validation
    val nameError: String? = null
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
    data class IronTemperatureChanged(val value: String) : YarnFormAction
    data class CareNotesChanged(val value: String) : YarnFormAction
    data class PhotoAdded(val uri: String) : YarnFormAction
    data class PhotoRemoved(val uri: String) : YarnFormAction
    data class NotesChanged(val value: String) : YarnFormAction
    object SaveYarn : YarnFormAction
    object ClearError : YarnFormAction
}

@HiltViewModel
class YarnFormViewModel @Inject constructor(
    private val yarnRepository: YarnRepository,
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
                            ironTemperature = care.ironTemperature ?: "",
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
            is YarnFormAction.IronTemperatureChanged ->
                _uiState.update { it.copy(ironTemperature = action.value) }
            is YarnFormAction.CareNotesChanged ->
                _uiState.update { it.copy(careNotes = action.value) }
            is YarnFormAction.PhotoAdded ->
                _uiState.update { it.copy(photos = it.photos + action.uri) }
            is YarnFormAction.PhotoRemoved ->
                _uiState.update { it.copy(photos = it.photos - action.uri) }
            is YarnFormAction.NotesChanged ->
                _uiState.update { it.copy(notes = action.value) }
            YarnFormAction.SaveYarn -> saveYarn()
            YarnFormAction.ClearError ->
                _uiState.update { it.copy(error = null) }
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
                val photosJson = json.encodeToString(stringListSerializer, current.photos)

                val careInstructions = CareInstructions(
                    machineWash = current.machineWash,
                    handWash = current.handWash,
                    dryClean = current.dryClean,
                    bleach = current.bleach,
                    tumbleDry = current.tumbleDry,
                    ironTemperature = current.ironTemperature.ifBlank { null },
                    notes = current.careNotes.ifBlank { null }
                )
                val careJson = json.encodeToString(CareInstructions.serializer(), careInstructions)

                val now = System.currentTimeMillis()

                if (current.isEditMode && yarnId != null) {
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
