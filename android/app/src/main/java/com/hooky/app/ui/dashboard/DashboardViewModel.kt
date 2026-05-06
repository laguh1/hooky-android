package com.hooky.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.repository.StitchRepository
import com.hooky.app.data.repository.YarnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    val inProgressPieces: List<PieceEntity> = emptyList(),
    val recentlyFinished: List<PieceEntity> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    private val yarnRepository: YarnRepository,
    private val stitchRepository: StitchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
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
                pieceRepository.getSoldRevenue()
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
