package com.hooky.app.ui.needles.form

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.data.repository.NeedleRepository
import com.hooky.app.data.scanner.ScanMode
import com.hooky.app.data.scanner.YarnLabelScannerService
import com.hooky.app.domain.model.NeedleScanResult
import com.hooky.app.domain.model.enums.NeedleType
import com.hooky.app.util.PhotoStorageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

data class NeedleFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false,
    // Form fields
    val name: String = "",
    val type: NeedleType = NeedleType.CROCHET_HOOK,
    val sizeMm: String = "",
    val sizeLabel: String = "",
    val material: String = "",
    val brand: String = "",
    val quantity: String = "1",
    val notes: String = "",
    val photos: List<String> = emptyList(),
    // Validation
    val nameError: String? = null,
    // Scan
    val isScanning: Boolean = false,
    val scanMode: ScanMode = ScanMode.NONE,
    val needleScanResult: NeedleScanResult? = null
)

sealed interface NeedleFormAction {
    data class NameChanged(val value: String) : NeedleFormAction
    data class TypeChanged(val value: NeedleType) : NeedleFormAction
    data class SizeMmChanged(val value: String) : NeedleFormAction
    data class SizeLabelChanged(val value: String) : NeedleFormAction
    data class MaterialChanged(val value: String) : NeedleFormAction
    data class BrandChanged(val value: String) : NeedleFormAction
    data class QuantityChanged(val value: String) : NeedleFormAction
    data class NotesChanged(val value: String) : NeedleFormAction
    data class PhotoAdded(val tempPath: String) : NeedleFormAction
    data class PhotoReceived(val tempPath: String) : NeedleFormAction
    data class PhotoRemoved(val path: String) : NeedleFormAction
    data class PhotoReplaced(val oldPath: String, val newPath: String) : NeedleFormAction
    object ScanSizeRequested : NeedleFormAction
    object ApplyScanResult : NeedleFormAction
    object DismissScanResult : NeedleFormAction
    object SaveNeedle : NeedleFormAction
    object ClearError : NeedleFormAction
}

@HiltViewModel
class NeedleFormViewModel @Inject constructor(
    private val needleRepository: NeedleRepository,
    private val scannerService: YarnLabelScannerService,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val needleId: Int? = savedStateHandle.get<Int>("id")

    private val _uiState = MutableStateFlow(NeedleFormUiState())
    val uiState: StateFlow<NeedleFormUiState> = _uiState.asStateFlow()

    init {
        if (needleId != null) {
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                needleRepository.getNeedleById(needleId)
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                    .first()
                    ?.let { needle -> populateForm(needle) }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun populateForm(needle: NeedleEntity) {
        val photos = try {
            json.decodeFromString<List<String>>(needle.photos)
        } catch (_: Exception) { emptyList() }

        val type = try {
            NeedleType.valueOf(needle.type)
        } catch (_: Exception) { NeedleType.CROCHET_HOOK }

        _uiState.update {
            it.copy(
                isEditMode = true,
                name = needle.name,
                type = type,
                sizeMm = needle.sizeMm?.toString() ?: "",
                sizeLabel = needle.sizeLabel ?: "",
                material = needle.material ?: "",
                brand = needle.brand ?: "",
                quantity = needle.quantity?.toString() ?: "1",
                notes = needle.notes ?: "",
                photos = photos
            )
        }
    }

    fun onAction(action: NeedleFormAction) {
        when (action) {
            is NeedleFormAction.NameChanged -> _uiState.update { it.copy(name = action.value, nameError = null) }
            is NeedleFormAction.TypeChanged -> _uiState.update { it.copy(type = action.value) }
            is NeedleFormAction.SizeMmChanged -> _uiState.update { it.copy(sizeMm = action.value) }
            is NeedleFormAction.SizeLabelChanged -> _uiState.update { it.copy(sizeLabel = action.value) }
            is NeedleFormAction.MaterialChanged -> _uiState.update { it.copy(material = action.value) }
            is NeedleFormAction.BrandChanged -> _uiState.update { it.copy(brand = action.value) }
            is NeedleFormAction.QuantityChanged -> _uiState.update { it.copy(quantity = action.value) }
            is NeedleFormAction.NotesChanged -> _uiState.update { it.copy(notes = action.value) }
            is NeedleFormAction.PhotoAdded -> {
                viewModelScope.launch { addPhoto(action.tempPath) }
            }
            is NeedleFormAction.PhotoReceived -> {
                if (_uiState.value.scanMode == ScanMode.HOOK) {
                    scanSize(action.tempPath)
                } else {
                    viewModelScope.launch { addPhoto(action.tempPath) }
                }
            }
            is NeedleFormAction.PhotoRemoved -> {
                _uiState.update { it.copy(photos = it.photos - action.path) }
                PhotoStorageUtil.deletePhoto(action.path)
            }
            is NeedleFormAction.PhotoReplaced ->
                _uiState.update { state ->
                    state.copy(photos = state.photos.map { if (it == action.oldPath) action.newPath else it })
                }
            NeedleFormAction.ScanSizeRequested ->
                _uiState.update { it.copy(scanMode = ScanMode.HOOK) }
            NeedleFormAction.ApplyScanResult -> {
                val result = _uiState.value.needleScanResult ?: return
                _uiState.update { it.copy(sizeMm = result.sizeMm, needleScanResult = null, scanMode = ScanMode.NONE) }
            }
            NeedleFormAction.DismissScanResult ->
                _uiState.update { it.copy(needleScanResult = null, scanMode = ScanMode.NONE) }
            NeedleFormAction.SaveNeedle -> saveNeedle()
            NeedleFormAction.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun scanSize(path: String) {
        val mode = if (_uiState.value.type == NeedleType.CROCHET_HOOK) ScanMode.HOOK else ScanMode.NEEDLE
        _uiState.update { it.copy(isScanning = true, scanMode = ScanMode.NONE) }
        viewModelScope.launch {
            try {
                val result = scannerService.scanNeedleFromPath(path, mode)
                if (result != null) {
                    _uiState.update { it.copy(isScanning = false, needleScanResult = result) }
                } else {
                    _uiState.update { it.copy(isScanning = false, error = "Couldn't read size — try a clearer, closer photo.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = "Scan failed: ${e.message}") }
            }
        }
    }

    private suspend fun addPhoto(tempPath: String) {
        val entityId = if (_uiState.value.isEditMode && needleId != null) {
            needleRepository.getNeedleById(needleId).first()?.needleId ?: "temp"
        } else "temp"

        val finalPath = try {
            PhotoStorageUtil.movePhotoToEntity(context, tempPath, "needles", entityId)
        } catch (_: Exception) {
            tempPath
        }
        _uiState.update { it.copy(photos = it.photos + finalPath) }
    }

    private fun saveNeedle() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val stringListSerializer = ListSerializer(serializer<String>())
                val photosJson = json.encodeToString(stringListSerializer, state.photos)
                val now = System.currentTimeMillis()

                if (state.isEditMode && needleId != null) {
                    val existing = needleRepository.getNeedleById(needleId).first()
                        ?: throw Exception("Needle not found")
                    needleRepository.updateNeedle(
                        existing.copy(
                            name = state.name.trim(),
                            type = state.type.name,
                            sizeMm = state.sizeMm.toFloatOrNull(),
                            sizeLabel = state.sizeLabel.trim().ifBlank { null },
                            material = state.material.trim().ifBlank { null },
                            brand = state.brand.trim().ifBlank { null },
                            quantity = state.quantity.toIntOrNull(),
                            notes = state.notes.trim().ifBlank { null },
                            photos = photosJson,
                            updatedAt = now
                        )
                    )
                } else {
                    val needleEntityId = needleRepository.generateNextNeedleId()
                    // Re-move photos to the real entity folder now that we have the ID
                    val finalPhotos = state.photos.map { path ->
                        if (path.contains("/temp/")) {
                            try {
                                PhotoStorageUtil.movePhotoToEntity(context, path, "needles", needleEntityId)
                            } catch (_: Exception) { path }
                        } else path
                    }
                    val finalPhotosJson = json.encodeToString(stringListSerializer, finalPhotos)
                    needleRepository.insertNeedle(
                        NeedleEntity(
                            needleId = needleEntityId,
                            name = state.name.trim(),
                            type = state.type.name,
                            sizeMm = state.sizeMm.toFloatOrNull(),
                            sizeLabel = state.sizeLabel.trim().ifBlank { null },
                            material = state.material.trim().ifBlank { null },
                            brand = state.brand.trim().ifBlank { null },
                            quantity = state.quantity.toIntOrNull() ?: 1,
                            notes = state.notes.trim().ifBlank { null },
                            photos = finalPhotosJson,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                }
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
