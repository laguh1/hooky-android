package com.hooky.app.ui.pieces.form

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.data.premium.PremiumManager
import com.hooky.app.data.repository.NeedleRepository
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.repository.StitchRepository
import com.hooky.app.data.repository.YarnRepository
import com.hooky.app.data.scanner.ScanMode
import com.hooky.app.data.scanner.YarnLabelScannerService
import com.hooky.app.domain.model.NeedleScanResult
import com.hooky.app.domain.model.YarnUsage
import com.hooky.app.domain.model.enums.Destination
import com.hooky.app.domain.model.enums.PieceType
import com.hooky.app.domain.model.enums.WorkStatus
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
    val workMinutes: String = "",
    val hookSizeMm: String = "",
    val photos: List<String> = emptyList(),
    val price: String = "",
    val materialCost: String = "",
    val giftRecipient: String = "",
    val salePlatform: String = "",
    val saleLink: String = "",
    val soldDate: String = "",
    val soldPrice: String = "",
    val quantityTotal: String = "1",
    val quantitySold: Int = 0,
    val yarnsUsed: List<YarnUsage> = emptyList(),
    val stitchesUsed: List<String> = emptyList(),
    val needlesUsed: List<String> = emptyList(),
    val notes: String = "",
    val rowCount: String = "0",
    val targetRowCount: String = "",
    val hasUnsavedChanges: Boolean = false,
    // Validation
    val nameError: String? = null,
    // Hook scanning
    val scanMode: ScanMode = ScanMode.NONE,
    val isScanning: Boolean = false,
    val needleScanResult: NeedleScanResult? = null,
    // Library pickers
    val availableYarns: List<YarnEntity> = emptyList(),
    val availableStitches: List<StitchEntity> = emptyList(),
    val availableNeedles: List<NeedleEntity> = emptyList(),
    val salePlatformSuggestions: List<String> = emptyList(),
)

// Sum of (yarn's price paid × balls used) across every yarn attached to the piece —
// the material cost the "Suggested Price" calculation is meant to consider automatically.
fun yarnMaterialCost(yarnsUsed: List<YarnUsage>, availableYarns: List<YarnEntity>): Float {
    val priceById = availableYarns.associate { it.yarnId to (it.pricePaid ?: 0f) }
    return yarnsUsed.sumOf { usage -> ((priceById[usage.yarnId] ?: 0f) * usage.balls).toDouble() }.toFloat()
}

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
    data class WorkMinutesChanged(val value: String) : PieceFormAction
    data class HookSizeMmChanged(val value: String) : PieceFormAction
    data class PhotoAdded(val uri: String) : PieceFormAction
    data class PhotoReceived(val path: String) : PieceFormAction
    data class PhotoRemoved(val uri: String) : PieceFormAction
    data class PhotoReplaced(val oldPath: String, val newPath: String) : PieceFormAction
    object ScanHookRequested : PieceFormAction
    object ScanYarnLabelRequested : PieceFormAction
    object ApplyNeedleScanResult : PieceFormAction
    object DismissNeedleScanResult : PieceFormAction
    data class PriceChanged(val value: String) : PieceFormAction
    data class MaterialCostChanged(val value: String) : PieceFormAction
    data class GiftRecipientChanged(val value: String) : PieceFormAction
    data class SalePlatformChanged(val value: String) : PieceFormAction
    data class SaleLinkChanged(val value: String) : PieceFormAction
    data class SoldDateChanged(val value: String) : PieceFormAction
    data class SoldPriceChanged(val value: String) : PieceFormAction
    data class QuantityTotalChanged(val value: String) : PieceFormAction
    object IncrementQuantitySold : PieceFormAction
    object DecrementQuantitySold : PieceFormAction
    data class NotesChanged(val value: String) : PieceFormAction
    data class RowCountChanged(val value: String) : PieceFormAction
    data class TargetRowCountChanged(val value: String) : PieceFormAction
    data class YarnsUsedChanged(val ids: List<String>) : PieceFormAction
    data class YarnBallsChanged(val yarnId: String, val balls: Float) : PieceFormAction
    object ApplyYarnMaterialCost : PieceFormAction
    data class StitchesUsedChanged(val ids: List<String>) : PieceFormAction
    data class NeedlesUsedChanged(val ids: List<String>) : PieceFormAction
    object SavePiece : PieceFormAction
    object ClearError : PieceFormAction
}

@HiltViewModel
class PieceFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pieceRepository: PieceRepository,
    private val yarnRepository: YarnRepository,
    private val stitchRepository: StitchRepository,
    private val needleRepository: NeedleRepository,
    private val labelScannerService: YarnLabelScannerService,
    private val premiumManager: PremiumManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pieceId: Int? = savedStateHandle["id"]

    companion object {
        const val MAX_FREE_PHOTOS = 5
    }

    private val _uiState = MutableStateFlow(
        PieceFormUiState(isEditMode = pieceId != null)
    )
    val uiState: StateFlow<PieceFormUiState> = _uiState.asStateFlow()

    init {
        if (pieceId != null) {
            loadExistingPiece(pieceId)
        }

        viewModelScope.launch {
            yarnRepository.getAllYarns()
                .catch { }
                .collect { yarns -> _uiState.update { it.copy(availableYarns = yarns) } }
        }

        viewModelScope.launch {
            stitchRepository.getAllStitches()
                .catch { }
                .collect { stitches -> _uiState.update { it.copy(availableStitches = stitches) } }
        }

        viewModelScope.launch {
            needleRepository.getAllNeedles()
                .catch { }
                .collect { needles -> _uiState.update { it.copy(availableNeedles = needles) } }
        }

        viewModelScope.launch {
            try {
                val suggestions = pieceRepository.getDistinctSalePlatforms()
                _uiState.update { it.copy(salePlatformSuggestions = suggestions) }
            } catch (_: Exception) { }
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
                        kotlinx.serialization.json.Json.decodeFromString<List<YarnUsage>>(piece.yarnsUsed)
                    } catch (_: Exception) {
                        // Pieces saved before per-yarn ball counts existed stored a plain
                        // List<String> of yarn IDs — read those as 1 ball each.
                        try {
                            kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.yarnsUsed)
                                .map { YarnUsage(yarnId = it, balls = 1f) }
                        } catch (_: Exception) { emptyList() }
                    }
                    val stitches = try {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.stitchesUsed)
                    } catch (_: Exception) { emptyList() }
                    val needles = try {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.needlesUsed)
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
                            workHours = piece.workHours?.let { kotlin.math.floor(it).toInt().toString() } ?: "",
                            workMinutes = piece.workHours?.let { h -> ((h - kotlin.math.floor(h)) * 60).toInt().let { if (it > 0) it.toString() else "" } } ?: "",
                            hookSizeMm = piece.hookSizeMm?.toString() ?: "",
                            photos = photos,
                            price = piece.price?.toString() ?: "",
                            materialCost = piece.materialCost?.toString() ?: "",
                            giftRecipient = piece.giftRecipient ?: "",
                            salePlatform = piece.salePlatform ?: "",
                            saleLink = piece.saleLink ?: "",
                            soldDate = piece.soldDate ?: "",
                            soldPrice = piece.soldPrice?.toString() ?: "",
                            quantityTotal = piece.quantityTotal.toString(),
                            quantitySold = piece.quantitySold,
                            yarnsUsed = yarns,
                            stitchesUsed = stitches,
                            needlesUsed = needles,
                            notes = piece.notes ?: "",
                            rowCount = piece.rowCount.toString(),
                            targetRowCount = piece.targetRowCount?.toString() ?: ""
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
        if (action !is PieceFormAction.ClearError && action !is PieceFormAction.SavePiece) {
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
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
            is PieceFormAction.WorkMinutesChanged ->
                _uiState.update { it.copy(workMinutes = action.value) }
            is PieceFormAction.HookSizeMmChanged ->
                _uiState.update { it.copy(hookSizeMm = action.value) }
            is PieceFormAction.PhotoAdded -> {
                if (premiumManager.isPremium || _uiState.value.photos.size < MAX_FREE_PHOTOS)
                    _uiState.update { it.copy(photos = it.photos + action.uri) }
                else
                    _uiState.update { it.copy(error = "Free tier is limited to $MAX_FREE_PHOTOS photos per piece. Upgrade to Pro for unlimited photos.") }
            }
            is PieceFormAction.PhotoReceived -> {
                if (_uiState.value.scanMode == ScanMode.HOOK) {
                    scanHook(action.path)
                } else if (premiumManager.isPremium || _uiState.value.photos.size < MAX_FREE_PHOTOS) {
                    _uiState.update { it.copy(photos = it.photos + action.path) }
                }
            }
            is PieceFormAction.PhotoRemoved ->
                _uiState.update { it.copy(photos = it.photos - action.uri) }
            is PieceFormAction.PhotoReplaced ->
                _uiState.update { state ->
                    state.copy(photos = state.photos.map { if (it == action.oldPath) action.newPath else it })
                }
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
            is PieceFormAction.QuantityTotalChanged ->
                _uiState.update { current ->
                    val newTotal = action.value.toIntOrNull() ?: 1
                    current.copy(
                        quantityTotal = action.value,
                        quantitySold = current.quantitySold.coerceIn(0, maxOf(newTotal, 0))
                    )
                }
            PieceFormAction.IncrementQuantitySold ->
                _uiState.update { current ->
                    val total = current.quantityTotal.toIntOrNull() ?: 1
                    current.copy(quantitySold = (current.quantitySold + 1).coerceAtMost(maxOf(total, 0)))
                }
            PieceFormAction.DecrementQuantitySold ->
                _uiState.update { it.copy(quantitySold = (it.quantitySold - 1).coerceAtLeast(0)) }
            is PieceFormAction.NotesChanged ->
                _uiState.update { it.copy(notes = action.value) }
            is PieceFormAction.RowCountChanged ->
                _uiState.update { it.copy(rowCount = action.value) }
            is PieceFormAction.TargetRowCountChanged ->
                _uiState.update { it.copy(targetRowCount = action.value) }
            is PieceFormAction.YarnsUsedChanged ->
                _uiState.update { current ->
                    // Keep each still-selected yarn's existing ball count; new yarns
                    // default to 1 ball; deselected ones are dropped.
                    val byId = current.yarnsUsed.associateBy { it.yarnId }
                    current.copy(yarnsUsed = action.ids.map { id -> byId[id] ?: YarnUsage(yarnId = id, balls = 1f) })
                }
            is PieceFormAction.YarnBallsChanged ->
                _uiState.update { current ->
                    current.copy(yarnsUsed = current.yarnsUsed.map {
                        if (it.yarnId == action.yarnId) it.copy(balls = action.balls.coerceAtLeast(1f)) else it
                    })
                }
            PieceFormAction.ApplyYarnMaterialCost ->
                _uiState.update { current ->
                    val cost = yarnMaterialCost(current.yarnsUsed, current.availableYarns)
                    current.copy(materialCost = if (cost % 1f == 0f) cost.toInt().toString() else "%.2f".format(cost))
                }
            is PieceFormAction.StitchesUsedChanged ->
                _uiState.update { it.copy(stitchesUsed = action.ids) }
            is PieceFormAction.NeedlesUsedChanged ->
                _uiState.update { it.copy(needlesUsed = action.ids) }
            PieceFormAction.SavePiece -> savePiece()
            is PieceFormAction.ClearError ->
                _uiState.update { it.copy(error = null) }
            PieceFormAction.ScanHookRequested ->
                _uiState.update { it.copy(scanMode = ScanMode.HOOK) }
            PieceFormAction.ScanYarnLabelRequested ->
                _uiState.update { it.copy(scanMode = ScanMode.LABEL) }
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
                val result = labelScannerService.scanNeedleFromPath(path, ScanMode.HOOK)
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
                val yarnsJson = jsonEncoder.encodeToString(
                    kotlinx.serialization.builtins.ListSerializer(YarnUsage.serializer()),
                    current.yarnsUsed
                )
                val stitchesJson = jsonEncoder.encodeToString(stringListSerializer, current.stitchesUsed)
                val needlesJson = jsonEncoder.encodeToString(stringListSerializer, current.needlesUsed)
                val now = System.currentTimeMillis()
                val resolvedQuantityTotal = (current.quantityTotal.toIntOrNull() ?: 1).coerceAtLeast(1)
                val resolvedQuantitySold = current.quantitySold.coerceIn(0, resolvedQuantityTotal)

                if (current.isEditMode && pieceId != null) {
                    val permanentPhotos = movePhotosToStorage(context, current.photos, "pieces", pieceId.toString())
                    val photosJson = jsonEncoder.encodeToString(stringListSerializer, permanentPhotos)
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
                            workHours = run { val h = current.workHours.toFloatOrNull() ?: 0f; val m = current.workMinutes.toFloatOrNull() ?: 0f; if (h > 0 || m > 0) h + m / 60f else null },
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
                            needlesUsed = needlesJson,
                            notes = current.notes.ifBlank { null },
                            quantityTotal = resolvedQuantityTotal,
                            quantitySold = resolvedQuantitySold,
                            updatedAt = now
                        )
                        pieceRepository.updatePiece(updated)
                    }
                } else {
                    val newPieceId = pieceRepository.generateNextPieceId()
                    val permanentPhotos = movePhotosToStorage(context, current.photos, "pieces", newPieceId.toString())
                    val photosJson = jsonEncoder.encodeToString(stringListSerializer, permanentPhotos)
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
                        needlesUsed = needlesJson,
                        notes = current.notes.ifBlank { null },
                        workSessions = "[]",
                        createdAt = now,
                        updatedAt = now,
                        rowCount = current.rowCount.toIntOrNull() ?: 0,
                        targetRowCount = current.targetRowCount.toIntOrNull(),
                        quantityTotal = resolvedQuantityTotal,
                        quantitySold = resolvedQuantitySold
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
