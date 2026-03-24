package com.crochet.manager.ui.yarns.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crochet.manager.data.db.entity.YarnEntity
import com.crochet.manager.data.repository.YarnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class YarnFilter(val displayName: String) {
    ALL("All"),
    WOOL("Wool"),
    COTTON("Cotton"),
    ACRYLIC("Acrylic"),
    OTHER("Other")
}

data class YarnListUiState(
    val yarns: List<YarnEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: YarnFilter = YarnFilter.ALL
)

sealed interface YarnListAction {
    data class SearchQueryChanged(val query: String) : YarnListAction
    data class FilterSelected(val filter: YarnFilter) : YarnListAction
    data class ArchiveYarn(val id: Int) : YarnListAction
    object ClearError : YarnListAction
}

@HiltViewModel
class YarnListViewModel @Inject constructor(
    private val yarnRepository: YarnRepository
) : ViewModel() {

    // Raw unfiltered non-archived list from DB
    private val allYarns = MutableStateFlow<List<YarnEntity>>(emptyList())

    private val _uiState = MutableStateFlow(YarnListUiState())
    val uiState: StateFlow<YarnListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            yarnRepository.getAllYarns()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { yarns ->
                    val active = yarns.filter { !it.archived }
                    allYarns.value = active
                    _uiState.update { current ->
                        current.copy(
                            yarns = applyFilterAndSearch(
                                yarns = active,
                                filter = current.selectedFilter,
                                query = current.searchQuery
                            ),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onAction(action: YarnListAction) {
        when (action) {
            is YarnListAction.SearchQueryChanged -> {
                _uiState.update { current ->
                    current.copy(
                        searchQuery = action.query,
                        yarns = applyFilterAndSearch(
                            yarns = allYarns.value,
                            filter = current.selectedFilter,
                            query = action.query
                        )
                    )
                }
            }
            is YarnListAction.FilterSelected -> {
                _uiState.update { current ->
                    current.copy(
                        selectedFilter = action.filter,
                        yarns = applyFilterAndSearch(
                            yarns = allYarns.value,
                            filter = action.filter,
                            query = current.searchQuery
                        )
                    )
                }
            }
            is YarnListAction.ArchiveYarn -> {
                viewModelScope.launch {
                    try {
                        val today = java.time.LocalDate.now().toString()
                        yarnRepository.archiveYarn(action.id, today, null)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            YarnListAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun applyFilterAndSearch(
        yarns: List<YarnEntity>,
        filter: YarnFilter,
        query: String
    ): List<YarnEntity> {
        val filtered = when (filter) {
            YarnFilter.ALL -> yarns
            // Match if material name contains the filter display name (case-insensitive)
            else -> yarns.filter { it.material.contains(filter.displayName, ignoreCase = true) }
        }
        return if (query.isBlank()) {
            filtered
        } else {
            filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.brand?.contains(query, ignoreCase = true) == true ||
                    it.color.contains(query, ignoreCase = true)
            }
        }
    }
}
