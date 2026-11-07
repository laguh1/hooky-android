package com.hooky.app.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.repository.StitchRepository
import com.hooky.app.data.repository.YarnRepository
import com.hooky.app.domain.model.enums.WorkStatus
import com.hooky.app.domain.model.ballsUsedByYarn
import com.hooky.app.util.AppPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val pieceCount: Int = 0,
    val yarnCount: Int = 0,
    val stitchCount: Int = 0,
    val totalWorkHours: Float = 0f,
    val soldRevenue: Float = 0f,
    // Yarn still in stock: balls left (bought − used in pieces) and what they cost
    val stashBallsLeft: Float = 0f,
    val stashValue: Float = 0f,
    val inProgressPieces: List<PieceEntity> = emptyList(),
    val recentlyFinished: List<PieceEntity> = emptyList(),
    // Getting-started card (new users)
    val gettingStartedVisible: Boolean = false,
    val hasCountedRows: Boolean = false,
    // Piece the "use the counter" step opens: newest in progress, else newest piece
    val counterPieceId: Int? = null
) {
    val gettingStartedDoneCount: Int
        get() = listOf(pieceCount > 0, yarnCount > 0, hasCountedRows).count { it }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val pieceRepository: PieceRepository,
    private val yarnRepository: YarnRepository,
    private val stitchRepository: StitchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val prefs = AppPrefs.get(context)
    private val gettingStartedDismissed = MutableStateFlow(
        prefs.getBoolean(AppPrefs.GETTING_STARTED_DISMISSED, false)
    )

    init {
        loadDashboardData()
        loadGettingStarted()
    }

    private fun loadGettingStarted() {
        val newUser = prefs.getBoolean(AppPrefs.GETTING_STARTED_NEW_USER, false)
        viewModelScope.launch {
            combine(
                pieceRepository.getAllPieces(),
                yarnRepository.getTotalYarnCount(),
                gettingStartedDismissed
            ) { pieces, yarnCount, dismissed ->
                val active = pieces.filter { !it.archived }
                val counted = active.any { it.rowCount > 0 }
                val counterPiece = active
                    .filter { it.workStatus == WorkStatus.IN_PROGRESS.name }
                    .maxByOrNull { it.updatedAt }
                    ?: active.maxByOrNull { it.updatedAt }
                // New installs see the card; people who already used the app only if they have no pieces yet.
                val eligible = newUser || active.isEmpty()
                val allDone = active.isNotEmpty() && yarnCount > 0 && counted
                Triple(eligible && !dismissed && !allDone, counted, counterPiece?.id)
            }
                .catch { }
                .collect { (visible, counted, pieceId) ->
                    _uiState.update {
                        it.copy(gettingStartedVisible = visible, hasCountedRows = counted, counterPieceId = pieceId)
                    }
                }
        }
    }

    fun dismissGettingStarted() {
        prefs.edit().putBoolean(AppPrefs.GETTING_STARTED_DISMISSED, true).apply()
        gettingStartedDismissed.value = true
    }

    private fun loadDashboardData() {
        // Combine the 5 count/aggregate flows.
        // We chain two combine() calls because the typed 5-flow overload uses an Array<*>
        // lambda which loses generic type information. Instead, nest two combine() calls
        // to keep full type safety.
        viewModelScope.launch {
            combine(
                pieceRepository.getPieceCount(),
                yarnRepository.getTotalYarnCount(),
                stitchRepository.getTotalStitchCount()
            ) { pieceCount, yarnCount, stitchCount ->
                Triple(pieceCount, yarnCount, stitchCount)
            }.combine(
                pieceRepository.getTotalWorkHours()
            ) { counts, totalHours ->
                Pair(counts, totalHours)
            }.combine(
                // Same rule as the report's "Vendido" (SaleRevenue), so the two numbers match
                pieceRepository.getAllPieces().map { pieces ->
                    pieces.sumOf {
                        com.hooky.app.util.SaleRevenue.of(it.destination, it.archived, it.price, it.soldPrice, it.quantityTotal, it.quantitySold).sold.toDouble()
                    }.toFloat()
                }
            ) { (counts, totalHours), soldRevenue ->
                val (pieceCount, yarnCount, stitchCount) = counts
                _uiState.update { current ->
                    current.copy(
                        pieceCount = pieceCount,
                        yarnCount = yarnCount,
                        stitchCount = stitchCount,
                        totalWorkHours = totalHours ?: 0f,
                        soldRevenue = soldRevenue ?: 0f
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect {}
        }

        viewModelScope.launch {
            combine(yarnRepository.getAllYarns(), pieceRepository.getAllPieces()) { yarns, pieces ->
                val used = ballsUsedByYarn(pieces.filter { !it.archived }.map { it.yarnsUsed })
                var balls = 0f
                var value = 0f
                yarns.filter { !it.archived }.forEach { y ->
                    val owned = y.quantityOwned ?: return@forEach
                    val left = (owned - (used[y.yarnId] ?: 0f)).coerceAtLeast(0f)
                    balls += left
                    value += left * (y.pricePaid ?: 0f)
                }
                balls to value
            }
                .catch { }
                .collect { (balls, value) -> _uiState.update { it.copy(stashBallsLeft = balls, stashValue = value) } }
        }

        // In-progress pieces — marks loading done once received
        viewModelScope.launch {
            pieceRepository.getInProgressPieces()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { pieces ->
                    _uiState.update { it.copy(inProgressPieces = pieces, isLoading = false) }
                }
        }

        // Recently finished pieces
        viewModelScope.launch {
            pieceRepository.getRecentlyFinished(limit = 5)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { pieces ->
                    _uiState.update { it.copy(recentlyFinished = pieces) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
