package com.crochet.manager.ui.stitches.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crochet.manager.data.db.entity.StitchEntity
import com.crochet.manager.data.repository.StitchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StitchFilter(val displayName: String) {
    ALL("All"),
    BASIC("Basic"),
    TEXTURED("Textured"),
    LACE("Lace"),
    SPECIALTY("Specialty")
}

data class StitchListUiState(
    val stitches: List<StitchEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: StitchFilter = StitchFilter.ALL
)

sealed interface StitchListAction {
    data class SearchQueryChanged(val query: String) : StitchListAction
    data class FilterSelected(val filter: StitchFilter) : StitchListAction
    data class ArchiveStitch(val id: Int) : StitchListAction
    object ClearError : StitchListAction
}

@HiltViewModel
class StitchListViewModel @Inject constructor(
    private val stitchRepository: StitchRepository
) : ViewModel() {

    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    // Raw unfiltered non-archived list from DB
    private val allStitches = MutableStateFlow<List<StitchEntity>>(emptyList())

    private val _uiState = MutableStateFlow(StitchListUiState())
    val uiState: StateFlow<StitchListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            stitchRepository.getAllStitches()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { stitches ->
                    val active = stitches.filter { !it.archived }
                    allStitches.value = active
                    _uiState.update { current ->
                        current.copy(
                            stitches = applyFilterAndSearch(
                                stitches = active,
                                filter = current.selectedFilter,
                                query = current.searchQuery
                            ),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onAction(action: StitchListAction) {
        when (action) {
            is StitchListAction.SearchQueryChanged -> {
                _uiState.update { current ->
                    current.copy(
                        searchQuery = action.query,
                        stitches = applyFilterAndSearch(
                            stitches = allStitches.value,
                            filter = current.selectedFilter,
                            query = action.query
                        )
                    )
                }
            }
            is StitchListAction.FilterSelected -> {
                _uiState.update { current ->
                    current.copy(
                        selectedFilter = action.filter,
                        stitches = applyFilterAndSearch(
                            stitches = allStitches.value,
                            filter = action.filter,
                            query = current.searchQuery
                        )
                    )
                }
            }
            is StitchListAction.ArchiveStitch -> {
                viewModelScope.launch {
                    try {
                        val today = java.time.LocalDate.now().toString()
                        stitchRepository.archiveStitch(action.id, today, null)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            StitchListAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun parseAliases(aliasesJson: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(aliasesJson)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun applyFilterAndSearch(
        stitches: List<StitchEntity>,
        filter: StitchFilter,
        query: String
    ): List<StitchEntity> {
        val filtered = when (filter) {
            StitchFilter.ALL -> stitches
            else -> stitches.filter { stitch ->
                stitch.category?.contains(filter.displayName, ignoreCase = true) == true
            }
        }
        return if (query.isBlank()) {
            filtered
        } else {
            filtered.filter { stitch ->
                stitch.name.contains(query, ignoreCase = true) ||
                    parseAliases(stitch.nameAliases).any { alias ->
                        alias.contains(query, ignoreCase = true)
                    }
            }
        }
    }
}
