package com.crochet.manager.ui.pieces.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crochet.manager.data.db.entity.PieceEntity
import com.crochet.manager.data.repository.PieceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PieceDetailUiState(
    val piece: PieceEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showArchiveDialog: Boolean = false
)

sealed interface PieceDetailAction {
    object ShowArchiveDialog : PieceDetailAction
    object HideArchiveDialog : PieceDetailAction
    data class ConfirmArchive(val reason: String?) : PieceDetailAction
    object ClearError : PieceDetailAction
}

@HiltViewModel
class PieceDetailViewModel @Inject constructor(
    private val pieceRepository: PieceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pieceId: Int = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(PieceDetailUiState())
    val uiState: StateFlow<PieceDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pieceRepository.getPieceById(pieceId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { piece ->
                    _uiState.update { it.copy(piece = piece, isLoading = false) }
                }
        }
    }

    fun onAction(action: PieceDetailAction) {
        when (action) {
            PieceDetailAction.ShowArchiveDialog -> {
                _uiState.update { it.copy(showArchiveDialog = true) }
            }
            PieceDetailAction.HideArchiveDialog -> {
                _uiState.update { it.copy(showArchiveDialog = false) }
            }
            is PieceDetailAction.ConfirmArchive -> {
                _uiState.update { it.copy(showArchiveDialog = false) }
                viewModelScope.launch {
                    try {
                        val today = LocalDate.now().toString()
                        pieceRepository.archivePiece(pieceId, today, action.reason)
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                }
            }
            PieceDetailAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }
}
