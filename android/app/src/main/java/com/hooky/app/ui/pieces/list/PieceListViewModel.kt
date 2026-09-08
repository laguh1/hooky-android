package com.hooky.app.ui.pieces.list

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
    val selectedFilter: PieceFilter = PieceFilter.ALL
)

sealed interface PieceListAction {
    data class SearchQueryChanged(val query: String) : PieceListAction
    data class FilterSelected(val filter: PieceFilter) : PieceListAction
    data class ArchivePiece(val id: Int) : PieceListAction
    object ClearError : PieceListAction
}

@HiltViewModel
class PieceListViewModel @Inject constructor(
    private val pieceRepository: PieceRepository
) : ViewModel() {

    // Raw unfiltered list from DB
    private val allPieces = MutableStateFlow<List<PieceEntity>>(emptyList())

    private val _uiState = MutableStateFlow(PieceListUiState())
    val uiState: StateFlow<PieceListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pieceRepository.getAllPieces()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { pieces ->
                    allPieces.value = pieces.filter { !it.archived }
                    _uiState.update { current ->
                        current.copy(
                            pieces = applyFilterAndSearch(
                                pieces = pieces.filter { !it.archived },
                                filter = current.selectedFilter,
                                query = current.searchQuery
                            ),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onAction(action: PieceListAction) {
        when (action) {
            is PieceListAction.SearchQueryChanged -> {
                _uiState.update { current ->
                    current.copy(
                        searchQuery = action.query,
                        pieces = applyFilterAndSearch(
                            pieces = allPieces.value,
                            filter = current.selectedFilter,
                            query = action.query
                        )
                    )
                }
            }
            is PieceListAction.FilterSelected -> {
                _uiState.update { current ->
                    current.copy(
                        selectedFilter = action.filter,
                        pieces = applyFilterAndSearch(
                            pieces = allPieces.value,
                            filter = action.filter,
                            query = current.searchQuery
                        )
                    )
                }
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
