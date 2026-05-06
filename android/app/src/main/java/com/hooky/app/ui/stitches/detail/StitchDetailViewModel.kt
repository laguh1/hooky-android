package com.hooky.app.ui.stitches.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.data.repository.StitchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StitchDetailUiState(
    val stitch: StitchEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showArchiveDialog: Boolean = false
)

sealed interface StitchDetailAction {
    object ShowArchiveDialog : StitchDetailAction
    object HideArchiveDialog : StitchDetailAction
    data class ConfirmArchive(val reason: String?) : StitchDetailAction
    object ClearError : StitchDetailAction
}

@HiltViewModel
class StitchDetailViewModel @Inject constructor(
    private val stitchRepository: StitchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stitchId: Int = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(StitchDetailUiState())
    val uiState: StateFlow<StitchDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            stitchRepository.getStitchById(stitchId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { stitch ->
                    _uiState.update { it.copy(stitch = stitch, isLoading = false) }
                }
        }
    }

    fun onAction(action: StitchDetailAction) {
        when (action) {
            StitchDetailAction.ShowArchiveDialog -> {
                _uiState.update { it.copy(showArchiveDialog = true) }
            }
            StitchDetailAction.HideArchiveDialog -> {
                _uiState.update { it.copy(showArchiveDialog = false) }
            }
            is StitchDetailAction.ConfirmArchive -> {
                _uiState.update { it.copy(showArchiveDialog = false) }
                viewModelScope.launch {
                    try {
                        val today = LocalDate.now().toString()
                        stitchRepository.archiveStitch(stitchId, today, action.reason)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            StitchDetailAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
}
