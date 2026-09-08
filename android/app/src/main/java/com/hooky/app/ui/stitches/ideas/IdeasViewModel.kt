package com.hooky.app.ui.stitches.ideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.IdeaEntity
import com.hooky.app.data.repository.IdeaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IdeasUiState(
    val ideas: List<IdeaEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class IdeasViewModel @Inject constructor(
    private val ideaRepository: IdeaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdeasUiState())
    val uiState: StateFlow<IdeasUiState> = _uiState.asStateFlow()

    fun load(locale: String) {
        viewModelScope.launch {
            ideaRepository.seedIfEmpty(locale)
            ideaRepository.getIdeas(locale)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { ideas ->
                    _uiState.update { it.copy(ideas = ideas, isLoading = false) }
                }
        }
    }

    fun addIdea(title: String, source: String, url: String, description: String?, locale: String) {
        viewModelScope.launch {
            ideaRepository.addIdea(
                IdeaEntity(
                    title = title.trim(),
                    source = source.trim(),
                    url = url.trim(),
                    description = description?.trim()?.takeIf { it.isNotEmpty() },
                    locale = locale,
                    isSeeded = false,
                    sortOrder = _uiState.value.ideas.size
                )
            )
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    fun deleteIdea(id: Int) {
        viewModelScope.launch { ideaRepository.deleteIdea(id) }
    }

    fun showAddDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun dismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
