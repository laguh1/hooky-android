package com.hooky.app.ui.pieces.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.db.entity.CounterEntity
import com.hooky.app.data.premium.PremiumManager
import com.hooky.app.domain.model.WorkSession
import com.hooky.app.data.repository.CounterRepository
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
    val isPremium: Boolean = false,
    val counters: List<CounterEntity> = emptyList(),
    val navigateToClone: Int? = null,
)

sealed interface PieceDetailAction {
    object ShowArchiveDialog : PieceDetailAction
    object HideArchiveDialog : PieceDetailAction
    data class ConfirmArchive(val reason: String?) : PieceDetailAction
    object ClonePiece : PieceDetailAction
    object ClearCloneNavigation : PieceDetailAction
    object ClearError : PieceDetailAction
    object IncrementRow : PieceDetailAction
    object DecrementRow : PieceDetailAction
    data class IncrementRowBy(val amount: Int) : PieceDetailAction
    data class SetRowCount(val count: Int, val target: Int?) : PieceDetailAction
    object StartTimer : PieceDetailAction
    object PauseTimer : PieceDetailAction
    object ResumeTimer : PieceDetailAction
    object StopTimer : PieceDetailAction
    data class SetWorkTime(val totalSeconds: Long) : PieceDetailAction
    data class ApplySuggestedPrice(val price: Float) : PieceDetailAction
    data class AddCounter(val name: String, val target: Int?) : PieceDetailAction
    data class DeleteCounter(val id: Int) : PieceDetailAction
    data class IncrementCounter(val id: Int) : PieceDetailAction
    data class DecrementCounter(val id: Int) : PieceDetailAction
    data class ResetCounter(val id: Int) : PieceDetailAction
    data class UpdateCounter(val counter: CounterEntity) : PieceDetailAction
}

@HiltViewModel
class PieceDetailViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    private val counterRepository: CounterRepository,
    private val premiumManager: PremiumManager,
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
    private var rowCountAtTimerStart: Int = -1

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

        viewModelScope.launch {
            counterRepository.getCountersForPiece(pieceId.toString())
                .catch { }
                .collect { counters -> _uiState.update { it.copy(counters = counters) } }
        }

        viewModelScope.launch {
            premiumManager.isPremiumFlow.collect { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
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
            PieceDetailAction.ClonePiece -> {
                viewModelScope.launch {
                    try {
                        val newId = pieceRepository.clonePiece(pieceId)
                        _uiState.update { it.copy(navigateToClone = newId) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            PieceDetailAction.ClearCloneNavigation ->
                _uiState.update { it.copy(navigateToClone = null) }
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
                rowCountAtTimerStart = piece.rowCount
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
                val durationMinutes = ((newTotal - piece.timerTotalSeconds)).toInt() / 60
                val rowsDone = if (rowCountAtTimerStart >= 0) piece.rowCount - rowCountAtTimerStart else 0
                rowCountAtTimerStart = -1
                viewModelScope.launch {
                    try {
                        pieceRepository.updateTimer(
                            id = pieceId,
                            totalSeconds = newTotal,
                            isRunning = false,
                            sessionStartAt = null,
                            workHours = workHours
                        )
                        if (durationMinutes > 0) {
                            val session = WorkSession(
                                date = java.time.LocalDate.now().toString(),
                                durationMinutes = durationMinutes,
                                rowsCompleted = if (rowsDone > 0) rowsDone else null
                            )
                            pieceRepository.appendWorkSession(pieceId, session, piece.workSessions)
                        }
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
            is PieceDetailAction.SetWorkTime -> {
                val piece = _uiState.value.piece ?: return
                val newTotal = action.totalSeconds.coerceAtLeast(0L)
                val workHours = newTotal / 3600f
                val isRunning = _uiState.value.timerState == TimerState.RUNNING
                if (isRunning) {
                    timerSessionStart = System.currentTimeMillis()
                }
                viewModelScope.launch {
                    try {
                        pieceRepository.updateTimer(
                            id = pieceId,
                            totalSeconds = newTotal,
                            isRunning = isRunning,
                            sessionStartAt = timerSessionStart,
                            workHours = workHours
                        )
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
                _uiState.update { it.copy(timerDisplaySeconds = newTotal) }
            }
            is PieceDetailAction.ApplySuggestedPrice -> {
                viewModelScope.launch {
                    try {
                        pieceRepository.updatePrice(pieceId, action.price)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            is PieceDetailAction.AddCounter -> {
                viewModelScope.launch {
                    try {
                        counterRepository.addCounter(pieceId.toString(), action.name, action.target)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            is PieceDetailAction.DeleteCounter -> {
                viewModelScope.launch {
                    try {
                        counterRepository.deleteCounter(action.id)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            is PieceDetailAction.IncrementCounter -> {
                val counter = _uiState.value.counters.find { it.id == action.id } ?: return
                viewModelScope.launch {
                    try {
                        counterRepository.updateCount(action.id, counter.count + 1)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            is PieceDetailAction.DecrementCounter -> {
                val counter = _uiState.value.counters.find { it.id == action.id } ?: return
                viewModelScope.launch {
                    try {
                        counterRepository.updateCount(action.id, maxOf(0, counter.count - 1))
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            is PieceDetailAction.ResetCounter -> {
                viewModelScope.launch {
                    try {
                        counterRepository.updateCount(action.id, 0)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            is PieceDetailAction.UpdateCounter -> {
                viewModelScope.launch {
                    try {
                        counterRepository.updateCounter(action.counter)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
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
