package com.hooky.app.ui.needles.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.data.repository.NeedleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NeedleDetailUiState(
    val needle: NeedleEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val archivedSuccessfully: Boolean = false
)

sealed interface NeedleDetailAction {
    data class Archive(val reason: String? = null) : NeedleDetailAction
    object ClearError : NeedleDetailAction
}

@HiltViewModel
class NeedleDetailViewModel @Inject constructor(
    private val needleRepository: NeedleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val needleId: Int = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(NeedleDetailUiState())
    val uiState: StateFlow<NeedleDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            needleRepository.getNeedleById(needleId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { needle ->
                    _uiState.update { it.copy(needle = needle, isLoading = false) }
                }
        }
    }

    fun onAction(action: NeedleDetailAction) {
        when (action) {
            is NeedleDetailAction.Archive -> {
                viewModelScope.launch {
                    try {
                        val today = java.time.LocalDate.now().toString()
                        needleRepository.archiveNeedle(needleId, today, action.reason)
                        _uiState.update { it.copy(archivedSuccessfully = true) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            NeedleDetailAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
}
