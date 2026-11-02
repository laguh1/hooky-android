package com.hooky.app.ui.yarns.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.data.repository.PieceRepository
import com.hooky.app.data.repository.YarnRepository
import com.hooky.app.domain.model.parseYarnUsages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class YarnDetailUiState(
    val yarn: YarnEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showArchiveDialog: Boolean = false,
    val navigateToClone: Int? = null,
    // Balls of this yarn used across all pieces (archived pieces don't count).
    // Worked out from the pieces each time rather than subtracted from the stock,
    // so editing or re-saving a piece can never count the same yarn twice.
    val ballsUsed: Float = 0f
)

sealed interface YarnDetailAction {
    object ShowArchiveDialog : YarnDetailAction
    object HideArchiveDialog : YarnDetailAction
    data class ConfirmArchive(val reason: String?) : YarnDetailAction
    object ClearError : YarnDetailAction
    object CloneYarn : YarnDetailAction
    object ClearCloneNavigation : YarnDetailAction
}

@HiltViewModel
class YarnDetailViewModel @Inject constructor(
    private val yarnRepository: YarnRepository,
    private val pieceRepository: PieceRepository,
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
        viewModelScope.launch {
            combine(yarnRepository.getYarnById(yarnId), pieceRepository.getAllPieces()) { yarn, pieces ->
                if (yarn == null) 0f
                else pieces.sumOf { piece ->
                    parseYarnUsages(piece.yarnsUsed).filter { it.yarnId == yarn.yarnId }.sumOf { it.balls.toDouble() }
                }.toFloat()
            }
                .catch { }
                .collect { used -> _uiState.update { it.copy(ballsUsed = used) } }
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
            YarnDetailAction.CloneYarn -> {
                viewModelScope.launch {
                    try {
                        val newId = yarnRepository.cloneYarn(yarnId)
                        _uiState.update { it.copy(navigateToClone = newId) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            YarnDetailAction.ClearCloneNavigation -> {
                _uiState.update { it.copy(navigateToClone = null) }
            }
        }
    }
}
