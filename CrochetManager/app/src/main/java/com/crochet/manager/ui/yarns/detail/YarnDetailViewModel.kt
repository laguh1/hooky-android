package com.crochet.manager.ui.yarns.detail

import androidx.lifecycle.SavedStateHandle
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
import java.time.LocalDate
import javax.inject.Inject

data class YarnDetailUiState(
    val yarn: YarnEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showArchiveDialog: Boolean = false
)

sealed interface YarnDetailAction {
    object ShowArchiveDialog : YarnDetailAction
    object HideArchiveDialog : YarnDetailAction
    data class ConfirmArchive(val reason: String?) : YarnDetailAction
    object ClearError : YarnDetailAction
}

@HiltViewModel
class YarnDetailViewModel @Inject constructor(
    private val yarnRepository: YarnRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val yarnId: Int = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(YarnDetailUiState())
    val uiState: StateFlow<YarnDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            yarnRepository.getYarnById(yarnId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { yarn ->
                    _uiState.update { it.copy(yarn = yarn, isLoading = false) }
                }
        }
    }

    fun onAction(action: YarnDetailAction) {
        when (action) {
            YarnDetailAction.ShowArchiveDialog -> {
                _uiState.update { it.copy(showArchiveDialog = true) }
            }
            YarnDetailAction.HideArchiveDialog -> {
                _uiState.update { it.copy(showArchiveDialog = false) }
            }
            is YarnDetailAction.ConfirmArchive -> {
                _uiState.update { it.copy(showArchiveDialog = false) }
                viewModelScope.launch {
                    try {
                        val today = LocalDate.now().toString()
                        yarnRepository.archiveYarn(yarnId, today, action.reason)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            YarnDetailAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
}
