package com.hooky.app.ui.pieces.list

import kotlinx.coroutines.flow.combine
import com.hooky.app.domain.model.parseYarnUsages
import com.hooky.app.domain.model.YarnColor
import com.hooky.app.domain.model.ColorFamily
import com.hooky.app.data.repository.YarnRepository
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.repository.PieceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.hooky.app.R
import javax.inject.Inject

enum class PieceFilter(@StringRes val labelResId: Int) {
    ALL(R.string.piece_filter_all),
    IN_PROGRESS(R.string.piece_filter_in_progress),
    FINISHED(R.string.piece_filter_finished)
}

data class PieceListUiState(
    val pieces: List<PieceEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: PieceFilter = PieceFilter.ALL,
    // A piece's colours come from the yarns it uses (most balls first), so nothing extra to type
    val pieceColors: Map<String, List<Int>> = emptyMap(),
    val colorFamilies: List<ColorFamily> = emptyList(),
    val selectedColor: ColorFamily? = null,
)

sealed interface PieceListAction {
    data class SearchQueryChanged(val query: String) : PieceListAction
    data class FilterSelected(val filter: PieceFilter) : PieceListAction
    data class ColorSelected(val family: ColorFamily?) : PieceListAction
    data class ArchivePiece(val id: Int) : PieceListAction
    object ClearError : PieceListAction
}

@HiltViewModel
class PieceListViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    private val yarnRepository: YarnRepository,
) : ViewModel() {

    // Raw unfiltered list from DB
    private val allPieces = MutableStateFlow<List<PieceEntity>>(emptyList())

    private val _uiState = MutableStateFlow(PieceListUiState())
    val uiState: StateFlow<PieceListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(pieceRepository.getAllPieces(), yarnRepository.getAllYarns()) { pieces, yarns -> pieces to yarns }
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { (pieces, yarns) ->
                    val active = pieces.filter { !it.archived }
                    allPieces.value = active
                    val colorByYarn = yarns.mapNotNull { y -> YarnColor.effective(y.colorHex, y.color)?.let { y.yarnId to it } }.toMap()
                    val pieceColors = active.associate { piece ->
                        piece.pieceId to parseYarnUsages(piece.yarnsUsed)
                            .sortedByDescending { it.balls }
                            .mapNotNull { colorByYarn[it.yarnId] }
                            .distinct()
                    }
                    _uiState.update { current ->
                        recompute(current.copy(
                            isLoading = false,
                            pieceColors = pieceColors,
                            colorFamilies = pieceColors.values.flatten().map { ColorFamily.of(it) }
                                .distinct().sortedBy { it.ordinal }
                        ))
                    }
                }
        }
    }

    fun onAction(action: PieceListAction) {
        when (action) {
            is PieceListAction.SearchQueryChanged -> {
                _uiState.update { recompute(it.copy(searchQuery = action.query)) }
            }
            is PieceListAction.FilterSelected -> {
                _uiState.update { recompute(it.copy(selectedFilter = action.filter)) }
            }
            is PieceListAction.ColorSelected -> {
                _uiState.update { recompute(it.copy(selectedColor = action.family)) }
            }
            is PieceListAction.ArchivePiece -> {
                viewModelScope.launch {
                    try {
                        val today = java.time.LocalDate.now().toString()
                        pieceRepository.archivePiece(action.id, today, null)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            PieceListAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun recompute(state: PieceListUiState): PieceListUiState {
        var list = applyFilterAndSearch(allPieces.value, state.selectedFilter, state.searchQuery)
        state.selectedColor?.let { family ->
            list = list.filter { p -> state.pieceColors[p.pieceId].orEmpty().any { ColorFamily.of(it) == family } }
        }
        return state.copy(pieces = list)
    }

    private fun applyFilterAndSearch(
        pieces: List<PieceEntity>,
        filter: PieceFilter,
        query: String
    ): List<PieceEntity> {
        val filtered = when (filter) {
            PieceFilter.ALL -> pieces
            PieceFilter.IN_PROGRESS -> pieces.filter { it.workStatus == "IN_PROGRESS" }
            PieceFilter.FINISHED -> pieces.filter { it.workStatus == "FINISHED" }
        }
        return if (query.isBlank()) {
            filtered
        } else {
            filtered.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
}
