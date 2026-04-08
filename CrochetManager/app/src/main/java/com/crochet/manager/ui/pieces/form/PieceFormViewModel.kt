package com.crochet.manager.ui.pieces.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crochet.manager.data.db.entity.PieceEntity
import com.crochet.manager.data.repository.PieceRepository
import com.crochet.manager.data.scanner.ScanMode
import com.crochet.manager.data.scanner.YarnLabelScannerService
import com.crochet.manager.domain.model.NeedleScanResult
import com.crochet.manager.domain.model.enums.Destination
import com.crochet.manager.domain.model.enums.PieceType
import com.crochet.manager.domain.model.enums.WorkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PieceFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false,
    // Form fields
    val name: String = "",
    val type: PieceType = PieceType.OTHER,
    val workStatus: WorkStatus = WorkStatus.IN_PROGRESS,
    val destination: Destination = Destination.FOR_SELF,
    val widthCm: String = "",
    val lengthCm: String = "",
    val dateStarted: String = "",
    val dateFinished: String = "",
    val workHours: String = "",
    val hookSizeMm: String = "",
    val photos: List<String> = emptyList(),
    val price: String = "",
    val materialCost: String = "",
    val giftRecipient: String = "",
    val salePlatform: String = "",
    val saleLink: String = "",
    val soldDate: String = "",
    val soldPrice: String = "",
    val yarnsUsed: List<String> = emptyList(),
    val stitchesUsed: List<String> = emptyList(),
    val notes: String = "",
    // Validation
    val nameError: String? = null,
    // Hook scanning
    val scanMode: ScanMode = ScanMode.NONE,
    val isScanning: Boolean = false,
    val needleScanResult: NeedleScanResult? = null
)

sealed interface PieceFormAction {
    data class NameChanged(val value: String) : PieceFormAction
    data class TypeChanged(val value: PieceType) : PieceFormAction
    data class WorkStatusChanged(val value: WorkStatus) : PieceFormAction
    data class DestinationChanged(val value: Destination) : PieceFormAction
    data class WidthCmChanged(val value: String) : PieceFormAction
    data class LengthCmChanged(val value: String) : PieceFormAction
    data class DateStartedChanged(val value: String) : PieceFormAction
    data class DateFinishedChanged(val value: String) : PieceFormAction
    data class WorkHoursChanged(val value: String) : PieceFormAction
    data class HookSizeMmChanged(val value: String) : PieceFormAction
    data class PhotoAdded(val uri: String) : PieceFormAction
    data class PhotoReceived(val path: String) : PieceFormAction
    data class PhotoRemoved(val uri: String) : PieceFormAction
    object ScanHookRequested : PieceFormAction
    object ApplyNeedleScanResult : PieceFormAction
    object DismissNeedleScanResult : PieceFormAction
    data class PriceChanged(val value: String) : PieceFormAction
    data class MaterialCostChanged(val value: String) : PieceFormAction
    data class GiftRecipientChanged(val value: String) : PieceFormAction
    data class SalePlatformChanged(val value: String) : PieceFormAction
    data class SaleLinkChanged(val value: String) : PieceFormAction
    data class SoldDateChanged(val value: String) : PieceFormAction
    data class SoldPriceChanged(val value: String) : PieceFormAction
    data class NotesChanged(val value: String) : PieceFormAction
    object SavePiece : PieceFormAction
    object ClearError : PieceFormAction
}

@HiltViewModel
class PieceFormViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    private val labelScannerService: YarnLabelScannerService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pieceId: Int? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(
        PieceFormUiState(isEditMode = pieceId != null)
    )
    val uiState: StateFlow<PieceFormUiState> = _uiState.asStateFlow()

    init {
        if (pieceId != null) {
            loadExistingPiece(pieceId)
        }
    }

    private fun loadExistingPiece(id: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val piece = pieceRepository.getPieceById(id)
                    .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                    .first()

                if (piece != null) {
                    val photos = try {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.photos)
                    } catch (_: Exception) { emptyList() }
                    val yarns = try {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.yarnsUsed)
                    } catch (_: Exception) { emptyList() }
                    val stitches = try {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.stitchesUsed)
                    } catch (_: Exception) { emptyList() }

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            name = piece.name,
                            type = try { PieceType.valueOf(piece.type) } catch (_: Exception) { PieceType.OTHER },
                            workStatus = try { WorkStatus.valueOf(piece.workStatus) } catch (_: Exception) { WorkStatus.IN_PROGRESS },
                            destination = try { Destination.valueOf(piece.destination) } catch (_: Exception) { Destination.FOR_SELF },
                            widthCm = piece.widthCm?.toString() ?: "",
                            lengthCm = piece.lengthCm?.toString() ?: "",
                            dateStarted = piece.dateStarted ?: "",
                            dateFinished = piece.dateFinished ?: "",
                            workHours = piece.workHours?.toString() ?: "",
                            hookSizeMm = piece.hookSizeMm?.toString() ?: "",
                            photos = photos,
                            price = piece.price?.toString() ?: "",
                            materialCost = piece.materialCost?.toString() ?: "",
                            giftRecipient = piece.giftRecipient ?: "",
                            salePlatform = piece.salePlatform ?: "",
                            saleLink = piece.saleLink ?: "",
                            soldDate = piece.soldDate ?: "",
                            soldPrice = piece.soldPrice?.toString() ?: "",
                            yarnsUsed = yarns,
                            stitchesUsed = stitches,
                            notes = piece.notes ?: ""
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

    fun onAction(action: PieceFormAction) {
        when (action) {
            is PieceFormAction.NameChanged ->
                _uiState.update { it.copy(name = action.value, nameError = null) }
            is PieceFormAction.TypeChanged ->
                _uiState.update { it.copy(type = action.value) }
            is PieceFormAction.WorkStatusChanged ->
                _uiState.update { it.copy(workStatus = action.value) }
            is PieceFormAction.DestinationChanged ->
                _uiState.update { it.copy(destination = action.value) }
            is PieceFormAction.WidthCmChanged ->
                _uiState.update { it.copy(widthCm = action.value) }
            is PieceFormAction.LengthCmChanged ->
                _uiState.update { it.copy(lengthCm = action.value) }
            is PieceFormAction.DateStartedChanged ->
                _uiState.update { it.copy(dateStarted = action.value) }
            is PieceFormAction.DateFinishedChanged ->
                _uiState.update { it.copy(dateFinished = action.value) }
            is PieceFormAction.WorkHoursChanged ->
                _uiState.update { it.copy(workHours = action.value) }
            is PieceFormAction.HookSizeMmChanged ->
                _uiState.update { it.copy(hookSizeMm = action.value) }
            is PieceFormAction.PhotoAdded ->
                _uiState.update { it.copy(photos = it.photos + action.uri) }
            is PieceFormAction.PhotoReceived -> {
                if (_uiState.value.scanMode == ScanMode.HOOK) {
                    scanHook(action.path)
                } else {
                    _uiState.update { it.copy(photos = it.photos + action.path) }
                }
            }
            is PieceFormAction.PhotoRemoved ->
                _uiState.update { it.copy(photos = it.photos - action.uri) }
            is PieceFormAction.PriceChanged ->
                _uiState.update { it.copy(price = action.value) }
            is PieceFormAction.MaterialCostChanged ->
                _uiState.update { it.copy(materialCost = action.value) }
            is PieceFormAction.GiftRecipientChanged ->
                _uiState.update { it.copy(giftRecipient = action.value) }
            is PieceFormAction.SalePlatformChanged ->
                _uiState.update { it.copy(salePlatform = action.value) }
            is PieceFormAction.SaleLinkChanged ->
                _uiState.update { it.copy(saleLink = action.value) }
            is PieceFormAction.SoldDateChanged ->
                _uiState.update { it.copy(soldDate = action.value) }
            is PieceFormAction.SoldPriceChanged ->
                _uiState.update { it.copy(soldPrice = action.value) }
            is PieceFormAction.NotesChanged ->
                _uiState.update { it.copy(notes = action.value) }
            PieceFormAction.SavePiece -> savePiece()
            is PieceFormAction.ClearError ->
                _uiState.update { it.copy(error = null) }
            PieceFormAction.ScanHookRequested ->
                _uiState.update { it.copy(scanMode = ScanMode.HOOK) }
            PieceFormAction.ApplyNeedleScanResult -> {
                val result = _uiState.value.needleScanResult ?: return
                _uiState.update { it.copy(hookSizeMm = result.sizeMm, needleScanResult = null, scanMode = ScanMode.NONE) }
            }
            PieceFormAction.DismissNeedleScanResult ->
                _uiState.update { it.copy(needleScanResult = null, scanMode = ScanMode.NONE) }
        }
    }

    private fun scanHook(path: String) {
        _uiState.update { it.copy(isScanning = true, scanMode = ScanMode.NONE) }
        viewModelScope.launch {
            try {
                val result = labelScannerService.scanNeedleFromPath(path)
                if (result != null) {
                    _uiState.update { it.copy(isScanning = false, needleScanResult = result, scanMode = ScanMode.HOOK) }
                } else {
                    _uiState.update { it.copy(isScanning = false, error = "Couldn't read size. Try a clearer, closer photo with good lighting.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = "Scan failed: ${e.message}") }
            }
        }
    }

    private fun savePiece() {
        val current = _uiState.value

        // Validate
        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, nameError = null) }

        viewModelScope.launch {
            try {
                val jsonEncoder = kotlinx.serialization.json.Json
                val stringListSerializer = kotlinx.serialization.builtins.ListSerializer(
                    kotlinx.serialization.serializer<String>()
                )
                val photosJson = jsonEncoder.encodeToString(stringListSerializer, current.photos)
                val yarnsJson = jsonEncoder.encodeToString(stringListSerializer, current.yarnsUsed)
                val stitchesJson = jsonEncoder.encodeToString(stringListSerializer, current.stitchesUsed)

                val now = System.currentTimeMillis()

                if (current.isEditMode && pieceId != null) {
                    // Load existing to preserve workSessions, pieceId, createdAt
                    val existing = pieceRepository.getPieceById(pieceId).first()
                    if (existing != null) {
                        val updated = existing.copy(
                            name = current.name,
                            type = current.type.name,
                            workStatus = current.workStatus.name,
                            destination = current.destination.name,
                            widthCm = current.widthCm.toFloatOrNull(),
                            lengthCm = current.lengthCm.toFloatOrNull(),
                            dateStarted = current.dateStarted.ifBlank { null },
                            dateFinished = current.dateFinished.ifBlank { null },
                            workHours = current.workHours.toFloatOrNull(),
                            hookSizeMm = current.hookSizeMm.toFloatOrNull(),
                            photos = photosJson,
                            price = current.price.toFloatOrNull(),
                            materialCost = current.materialCost.toFloatOrNull(),
                            giftRecipient = current.giftRecipient.ifBlank { null },
                            salePlatform = current.salePlatform.ifBlank { null },
                            saleLink = current.saleLink.ifBlank { null },
                            soldDate = current.soldDate.ifBlank { null },
                            soldPrice = current.soldPrice.toFloatOrNull(),
                            yarnsUsed = yarnsJson,
                            stitchesUsed = stitchesJson,
                            notes = current.notes.ifBlank { null },
                            updatedAt = now
                        )
                        pieceRepository.updatePiece(updated)
                    }
                } else {
                    val newPieceId = pieceRepository.generateNextPieceId()
                    val entity = PieceEntity(
                        pieceId = newPieceId,
                        name = current.name,
                        type = current.type.name,
                        workStatus = current.workStatus.name,
                        destination = current.destination.name,
                        widthCm = current.widthCm.toFloatOrNull(),
                        lengthCm = current.lengthCm.toFloatOrNull(),
                        dateStarted = current.dateStarted.ifBlank { null },
                        dateFinished = current.dateFinished.ifBlank { null },
                        workHours = current.workHours.toFloatOrNull(),
                        hookSizeMm = current.hookSizeMm.toFloatOrNull(),
                        photos = photosJson,
                        price = current.price.toFloatOrNull(),
                        materialCost = current.materialCost.toFloatOrNull(),
                        giftRecipient = current.giftRecipient.ifBlank { null },
                        salePlatform = current.salePlatform.ifBlank { null },
                        saleLink = current.saleLink.ifBlank { null },
                        soldDate = current.soldDate.ifBlank { null },
                        soldPrice = current.soldPrice.toFloatOrNull(),
                        yarnsUsed = yarnsJson,
                        stitchesUsed = stitchesJson,
                        notes = current.notes.ifBlank { null },
                        workSessions = "[]",
                        createdAt = now,
                        updatedAt = now
                    )
                    pieceRepository.insertPiece(entity)
                }

                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
