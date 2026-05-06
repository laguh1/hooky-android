package com.hooky.app.ui.pieces.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.repository.NeedleRepository
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.repository.StitchRepository
import com.hooky.app.data.repository.YarnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class TimerState { IDLE, RUNNING, PAUSED }

data class PieceDetailUiState(
    val piece: PieceEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showArchiveDialog: Boolean = false,
    val timerDisplaySeconds: Long = 0,
    val timerState: TimerState = TimerState.IDLE,
    val yarnNames: Map<String, String> = emptyMap(),
    val stitchNames: Map<String, String> = emptyMap(),
    val needleNames: Map<String, String> = emptyMap(),
)

sealed interface PieceDetailAction {
    object ShowArchiveDialog : PieceDetailAction
    object HideArchiveDialog : PieceDetailAction
    data class ConfirmArchive(val reason: String?) : PieceDetailAction
    object ClearError : PieceDetailAction
    object IncrementRow : PieceDetailAction
    object DecrementRow : PieceDetailAction
    data class IncrementRowBy(val amount: Int) : PieceDetailAction
    data class SetRowCount(val count: Int, val target: Int?) : PieceDetailAction
    object StartTimer : PieceDetailAction
    object PauseTimer : PieceDetailAction
    object ResumeTimer : PieceDetailAction
    object StopTimer : PieceDetailAction
}

@HiltViewModel
class PieceDetailViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    private val yarnRepository: YarnRepository,
    private val stitchRepository: StitchRepository,
    private val needleRepository: NeedleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pieceId: Int = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(PieceDetailUiState())
    val uiState: StateFlow<PieceDetailUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var timerSessionStart: Long? = null
    private var timerInitialized = false

    init {
        viewModelScope.launch {
            pieceRepository.getPieceById(pieceId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { piece ->
                    _uiState.update { it.copy(piece = piece, isLoading = false) }
                    if (!timerInitialized && piece != null) {
                        timerInitialized = true
                        if (piece.timerIsRunning && piece.timerSessionStartAt != null) {
                            timerSessionStart = piece.timerSessionStartAt
                            _uiState.update { it.copy(timerState = TimerState.RUNNING) }
                            startTicker()
                        } else {
                            _uiState.update {
                                it.copy(
                                    timerDisplaySeconds = piece.timerTotalSeconds,
                                    timerState = TimerState.IDLE
                                )
                            }
                        }
                    }
                }
        }

        // Build yarn name map
        viewModelScope.launch {
            yarnRepository.getAllYarns()
                .catch { }
                .collect { yarns ->
                    val map = yarns.associate { yarn ->
                        yarn.yarnId to "${yarn.name}${yarn.brand?.let { " (${it})" } ?: ""}"
                    }
                    _uiState.update { it.copy(yarnNames = map) }
                }
        }

        // Build stitch name map
        viewModelScope.launch {
            stitchRepository.getAllStitches()
                .catch { }
                .collect { stitches ->
                    val map = stitches.associate { stitch -> stitch.stitchId to stitch.name }
                    _uiState.update { it.copy(stitchNames = map) }
                }
        }

        // Build needle name map
        viewModelScope.launch {
            needleRepository.getAllNeedles()
                .catch { }
                .collect { needles ->
                    val map = needles.associate { needle ->
                        needle.needleId to "${needle.name}${needle.sizeMm?.let { " ${it}mm" } ?: ""}"
                    }
                    _uiState.update { it.copy(needleNames = map) }
                }
        }
    }

    fun onAction(action: PieceDetailAction) {
        when (action) {
            PieceDetailAction.ShowArchiveDialog ->
                _uiState.update { it.copy(showArchiveDialog = true) }
            PieceDetailAction.HideArchiveDialog ->
                _uiState.update { it.copy(showArchiveDialog = false) }
            is PieceDetailAction.ConfirmArchive -> {
                _uiState.update { it.copy(showArchiveDialog = false) }
                viewModelScope.launch {
                    try {
                        val today = LocalDate.now().toString()
                        pieceRepository.archivePiece(pieceId, today, action.reason)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            PieceDetailAction.ClearError ->
                _uiState.update { it.copy(error = null) }
            PieceDetailAction.IncrementRow -> updateRow { it + 1 }
            PieceDetailAction.DecrementRow -> updateRow { maxOf(0, it - 1) }
            is PieceDetailAction.IncrementRowBy -> updateRow { it + action.amount }
            is PieceDetailAction.SetRowCount -> {
                val piece = _uiState.value.piece ?: return
                viewModelScope.launch {
                    try {
                        pieceRepository.updateRowCounter(pieceId, action.count, action.target)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            PieceDetailAction.StartTimer -> {
                val piece = _uiState.value.piece ?: return
                val now = System.currentTimeMillis()
                timerSessionStart = now
                viewModelScope.launch {
                    try {
                        pieceRepository.updateTimer(
                            id = pieceId,
                            totalSeconds = piece.timerTotalSeconds,
                            isRunning = true,
                            sessionStartAt = now,
                            workHours = piece.workHours
                        )
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
                _uiState.update { it.copy(timerState = TimerState.RUNNING) }
                startTicker()
            }
            PieceDetailAction.PauseTimer -> {
                val piece = _uiState.value.piece ?: return
                timerJob?.cancel()
                timerJob = null
                val sessionStart = timerSessionStart ?: return
                val elapsed = (System.currentTimeMillis() - sessionStart) / 1000L
                val newTotal = piece.timerTotalSeconds + elapsed
                timerSessionStart = null
                viewModelScope.launch {
                    try {
                        pieceRepository.updateTimer(
                            id = pieceId,
                            totalSeconds = newTotal,
                            isRunning = false,
                            sessionStartAt = null,
                            workHours = piece.workHours
                        )
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
                _uiState.update {
                    it.copy(
                        timerDisplaySeconds = newTotal,
                        timerState = TimerState.PAUSED
                    )
                }
            }
            PieceDetailAction.ResumeTimer -> {
                val piece = _uiState.value.piece ?: return
                val now = System.currentTimeMillis()
                timerSessionStart = now
                viewModelScope.launch {
                    try {
                        pieceRepository.updateTimer(
                            id = pieceId,
                            totalSeconds = piece.timerTotalSeconds,
                            isRunning = true,
                            sessionStartAt = now,
                            workHours = piece.workHours
                        )
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
                _uiState.update { it.copy(timerState = TimerState.RUNNING) }
                startTicker()
            }
            PieceDetailAction.StopTimer -> {
                val piece = _uiState.value.piece ?: return
                timerJob?.cancel()
                timerJob = null
                val newTotal = if (_uiState.value.timerState == TimerState.RUNNING) {
                    val sessionStart = timerSessionStart ?: return
                    val elapsed = (System.currentTimeMillis() - sessionStart) / 1000L
                    piece.timerTotalSeconds + elapsed
                } else {
                    piece.timerTotalSeconds
                }
                timerSessionStart = null
                val workHours = newTotal / 3600f
                viewModelScope.launch {
                    try {
                        pieceRepository.updateTimer(
                            id = pieceId,
                            totalSeconds = newTotal,
                            isRunning = false,
                            sessionStartAt = null,
                            workHours = workHours
                        )
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
                _uiState.update {
                    it.copy(
                        timerDisplaySeconds = newTotal,
                        timerState = TimerState.IDLE
                    )
                }
            }
        }
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val piece = _uiState.value.piece ?: continue
                val sessionStart = timerSessionStart ?: continue
                val elapsed = (System.currentTimeMillis() - sessionStart) / 1000L
                _uiState.update { it.copy(timerDisplaySeconds = piece.timerTotalSeconds + elapsed) }
            }
        }
    }

    private fun updateRow(transform: (Int) -> Int) {
        val piece = _uiState.value.piece ?: return
        val newCount = transform(piece.rowCount)
        viewModelScope.launch {
            try {
                pieceRepository.updateRowCounter(pieceId, newCount, piece.targetRowCount)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
