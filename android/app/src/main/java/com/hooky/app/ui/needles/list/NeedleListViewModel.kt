package com.hooky.app.ui.needles.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.data.repository.NeedleRepository
import com.hooky.app.domain.model.enums.NeedleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NeedleFilter(val displayName: String) {
    ALL("All"),
    CROCHET_HOOK("Hooks"),
    KNITTING_NEEDLE("Knitting"),
    OTHER("Other")
}

data class NeedleListUiState(
    val needles: List<NeedleEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: NeedleFilter = NeedleFilter.ALL
)

sealed interface NeedleListAction {
    data class SearchQueryChanged(val query: String) : NeedleListAction
    data class FilterSelected(val filter: NeedleFilter) : NeedleListAction
    data class ArchiveNeedle(val id: Int) : NeedleListAction
    object ClearError : NeedleListAction
}

@HiltViewModel
class NeedleListViewModel @Inject constructor(
    private val needleRepository: NeedleRepository
) : ViewModel() {

    private val allNeedles = MutableStateFlow<List<NeedleEntity>>(emptyList())

    private val _uiState = MutableStateFlow(NeedleListUiState())
    val uiState: StateFlow<NeedleListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            needleRepository.getAllNeedles()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { needles ->
                    allNeedles.value = needles
                    _uiState.update { current ->
                        current.copy(
                            needles = applyFilterAndSearch(
                                needles = needles,
                                filter = current.selectedFilter,
                                query = current.searchQuery
                            ),
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onAction(action: NeedleListAction) {
        when (action) {
            is NeedleListAction.SearchQueryChanged -> {
                _uiState.update { current ->
                    current.copy(
                        searchQuery = action.query,
                        needles = applyFilterAndSearch(
                            needles = allNeedles.value,
                            filter = current.selectedFilter,
                            query = action.query
                        )
                    )
                }
            }
            is NeedleListAction.FilterSelected -> {
                _uiState.update { current ->
                    current.copy(
                        selectedFilter = action.filter,
                        needles = applyFilterAndSearch(
                            needles = allNeedles.value,
                            filter = action.filter,
                            query = current.searchQuery
                        )
                    )
                }
            }
            is NeedleListAction.ArchiveNeedle -> {
                viewModelScope.launch {
                    try {
                        val today = java.time.LocalDate.now().toString()
                        needleRepository.archiveNeedle(action.id, today, null)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            NeedleListAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun applyFilterAndSearch(
        needles: List<NeedleEntity>,
        filter: NeedleFilter,
        query: String
    ): List<NeedleEntity> {
        val filtered = when (filter) {
            NeedleFilter.ALL -> needles
            NeedleFilter.CROCHET_HOOK -> needles.filter {
                it.type == NeedleType.CROCHET_HOOK.name
            }
            NeedleFilter.KNITTING_NEEDLE -> needles.filter {
                it.type == NeedleType.KNITTING_NEEDLE.name ||
                    it.type == NeedleType.DPN.name ||
                    it.type == NeedleType.CIRCULAR.name
            }
            NeedleFilter.OTHER -> needles.filter {
                it.type == NeedleType.TAPESTRY_NEEDLE.name ||
                    it.type == NeedleType.CABLE_NEEDLE.name ||
                    it.type == NeedleType.OTHER.name
            }
        }
        return if (query.isBlank()) {
            filtered
        } else {
            filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.brand?.contains(query, ignoreCase = true) == true ||
                    it.sizeLabel?.contains(query, ignoreCase = true) == true ||
                    it.sizeMm?.toString()?.contains(query) == true
            }
        }
    }
}
