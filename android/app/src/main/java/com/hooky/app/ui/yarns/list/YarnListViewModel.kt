package com.hooky.app.ui.yarns.list

import com.hooky.app.domain.model.ColorFamily
import com.hooky.app.domain.model.YarnColor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.R
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.data.repository.YarnRepository
import com.hooky.app.domain.model.enums.Material
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// material is null for ALL (no filtering). labelRes drives the UI label — kept
// separate from the filter key so translating the label can never affect which
// yarns match, since yarn.material is always stored as the Material enum's
// language-independent name (e.g. "WOOL").
enum class YarnFilter(val material: Material?, val labelRes: Int) {
    ALL(null, R.string.yarn_filter_all),
    WOOL(Material.WOOL, R.string.yarn_filter_wool),
    COTTON(Material.COTTON, R.string.yarn_filter_cotton),
    ACRYLIC(Material.ACRYLIC, R.string.yarn_filter_acrylic),
    OTHER(Material.OTHER, R.string.yarn_filter_other)
}

data class YarnListUiState(
    val yarns: List<YarnEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: YarnFilter = YarnFilter.ALL,
    // Colour groups present in the stash (only these get a chip), the chosen one, and sorting
    val colorFamilies: List<ColorFamily> = emptyList(),
    val selectedColor: ColorFamily? = null,
    val sortByColor: Boolean = false,
)

sealed interface YarnListAction {
    data class SearchQueryChanged(val query: String) : YarnListAction
    data class FilterSelected(val filter: YarnFilter) : YarnListAction
    data class ColorSelected(val family: ColorFamily?) : YarnListAction
    object ToggleSortByColor : YarnListAction
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
                        recompute(current.copy(
                            isLoading = false,
                            colorFamilies = active.mapNotNull { colorOf(it)?.let { c -> ColorFamily.of(c) } }
                                .distinct().sortedBy { it.ordinal }
                        ))
                    }
                }
        }
    }

    fun onAction(action: YarnListAction) {
        when (action) {
            is YarnListAction.SearchQueryChanged -> {
                _uiState.update { recompute(it.copy(searchQuery = action.query)) }
            }
            is YarnListAction.FilterSelected -> {
                _uiState.update { recompute(it.copy(selectedFilter = action.filter)) }
            }
            is YarnListAction.ColorSelected -> {
                _uiState.update { recompute(it.copy(selectedColor = action.family)) }
            }
            YarnListAction.ToggleSortByColor -> {
                _uiState.update { recompute(it.copy(sortByColor = !it.sortByColor)) }
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

    private fun colorOf(yarn: YarnEntity): Int? = YarnColor.effective(yarn.colorHex, yarn.color)

    private fun recompute(state: YarnListUiState): YarnListUiState {
        var list = applyFilterAndSearch(allYarns.value, state.selectedFilter, state.searchQuery)
        state.selectedColor?.let { family ->
            list = list.filter { y -> colorOf(y)?.let { ColorFamily.of(it) } == family }
        }
        if (state.sortByColor) list = list.sortedBy { ColorFamily.sortKey(colorOf(it)) }
        return state.copy(yarns = list)
    }

    private fun applyFilterAndSearch(
        yarns: List<YarnEntity>,
        filter: YarnFilter,
        query: String
    ): List<YarnEntity> {
        val filtered = when (filter.material) {
            null -> yarns
            else -> yarns.filter { it.material == filter.material.name }
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
