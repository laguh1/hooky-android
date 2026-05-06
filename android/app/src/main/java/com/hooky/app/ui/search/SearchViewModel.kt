package com.hooky.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.repository.StitchRepository
import com.hooky.app.data.repository.YarnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val pieceResults: List<PieceEntity> = emptyList(),
    val yarnResults: List<YarnEntity> = emptyList(),
    val stitchResults: List<StitchEntity> = emptyList(),
    val hasSearched: Boolean = false
)

sealed interface SearchAction {
    data class QueryChanged(val query: String) : SearchAction
    object ClearQuery : SearchAction
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    private val yarnRepository: YarnRepository,
    private val stitchRepository: StitchRepository
) : ViewModel() {

    private val _queryFlow = MutableStateFlow("")

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _queryFlow
                .debounce(300L)
                .flatMapLatest { query ->
                    if (query.length < 2) {
                        flowOf(Triple(emptyList<PieceEntity>(), emptyList<YarnEntity>(), emptyList<StitchEntity>()))
                    } else {
                        combine(
                            pieceRepository.searchPieces(query),
                            yarnRepository.searchYarns(query),
                            stitchRepository.searchStitches(query)
                        ) { pieces, yarns, stitches ->
                            Triple(pieces, yarns, stitches)
                        }
                    }
                }
                .collect { (pieces, yarns, stitches) ->
                    val query = _queryFlow.value
                    _uiState.update {
                        it.copy(
                            pieceResults = pieces,
                            yarnResults = yarns,
                            stitchResults = stitches,
                            isLoading = false,
                            hasSearched = query.length >= 2
                        )
                    }
                }
        }
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.QueryChanged -> {
                val q = action.query
                _uiState.update { it.copy(query = q, isLoading = q.length >= 2) }
                _queryFlow.value = q
            }
            SearchAction.ClearQuery -> {
                _queryFlow.value = ""
                _uiState.update {
                    SearchUiState()
                }
            }
        }
    }
}
