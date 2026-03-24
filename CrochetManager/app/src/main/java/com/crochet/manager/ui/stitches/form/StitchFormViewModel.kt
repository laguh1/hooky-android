package com.crochet.manager.ui.stitches.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crochet.manager.data.db.entity.StitchEntity
import com.crochet.manager.data.repository.StitchRepository
import com.crochet.manager.domain.model.enums.Difficulty
import com.crochet.manager.domain.model.enums.StitchCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject

data class StitchFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false,
    val name: String = "",
    val nameAliases: String = "",        // comma-separated input, stored as JSON list
    val nameEs: String = "",
    val abbreviation: String = "",
    val category: StitchCategory? = null,
    val difficulty: Difficulty? = null,
    val description: String = "",
    val hookfullyLink: String = "",
    val instructionLink: String = "",
    val videoLink: String = "",
    val photos: List<String> = emptyList(),
    val notes: String = "",
    val nameError: String? = null,
    val descriptionError: String? = null
)

sealed interface StitchFormAction {
    data class NameChanged(val value: String) : StitchFormAction
    data class NameAliasesChanged(val value: String) : StitchFormAction
    data class NameEsChanged(val value: String) : StitchFormAction
    data class AbbreviationChanged(val value: String) : StitchFormAction
    data class CategoryChanged(val value: StitchCategory?) : StitchFormAction
    data class DifficultyChanged(val value: Difficulty?) : StitchFormAction
    data class DescriptionChanged(val value: String) : StitchFormAction
    data class HookfullyLinkChanged(val value: String) : StitchFormAction
    data class InstructionLinkChanged(val value: String) : StitchFormAction
    data class VideoLinkChanged(val value: String) : StitchFormAction
    data class PhotoAdded(val uri: String) : StitchFormAction
    data class PhotoRemoved(val uri: String) : StitchFormAction
    data class NotesChanged(val value: String) : StitchFormAction
    object SaveStitch : StitchFormAction
    object ClearError : StitchFormAction
}

@HiltViewModel
class StitchFormViewModel @Inject constructor(
    private val stitchRepository: StitchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stitchId: Int? = savedStateHandle["id"]

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(
        StitchFormUiState(isEditMode = stitchId != null)
    )
    val uiState: StateFlow<StitchFormUiState> = _uiState.asStateFlow()

    init {
        if (stitchId != null) {
            loadExistingStitch(stitchId)
        }
    }

    private fun loadExistingStitch(id: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val stitch = stitchRepository.getStitchById(id)
                    .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                    .first()

                if (stitch != null) {
                    val photos = try {
                        json.decodeFromString<List<String>>(stitch.photos)
                    } catch (_: Exception) { emptyList() }

                    val aliases = try {
                        json.decodeFromString<List<String>>(stitch.nameAliases).joinToString(", ")
                    } catch (_: Exception) { "" }

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            name = stitch.name,
                            nameAliases = aliases,
                            nameEs = stitch.nameEs ?: "",
                            abbreviation = stitch.abbreviation ?: "",
                            category = stitch.category?.let {
                                try { StitchCategory.valueOf(it) } catch (_: Exception) { null }
                            },
                            difficulty = stitch.difficulty?.let {
                                try { Difficulty.valueOf(it) } catch (_: Exception) { null }
                            },
                            description = stitch.description,
                            hookfullyLink = stitch.hookfullyLink ?: "",
                            instructionLink = stitch.instructionLink ?: "",
                            videoLink = stitch.videoLink ?: "",
                            photos = photos,
                            notes = stitch.notes ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onAction(action: StitchFormAction) {
        when (action) {
            is StitchFormAction.NameChanged ->
                _uiState.update { it.copy(name = action.value, nameError = null) }
            is StitchFormAction.NameAliasesChanged ->
                _uiState.update { it.copy(nameAliases = action.value) }
            is StitchFormAction.NameEsChanged ->
                _uiState.update { it.copy(nameEs = action.value) }
            is StitchFormAction.AbbreviationChanged ->
                _uiState.update { it.copy(abbreviation = action.value) }
            is StitchFormAction.CategoryChanged ->
                _uiState.update { it.copy(category = action.value) }
            is StitchFormAction.DifficultyChanged ->
                _uiState.update { it.copy(difficulty = action.value) }
            is StitchFormAction.DescriptionChanged ->
                _uiState.update { it.copy(description = action.value, descriptionError = null) }
            is StitchFormAction.HookfullyLinkChanged ->
                _uiState.update { it.copy(hookfullyLink = action.value) }
            is StitchFormAction.InstructionLinkChanged ->
                _uiState.update { it.copy(instructionLink = action.value) }
            is StitchFormAction.VideoLinkChanged ->
                _uiState.update { it.copy(videoLink = action.value) }
            is StitchFormAction.PhotoAdded ->
                _uiState.update { it.copy(photos = it.photos + action.uri) }
            is StitchFormAction.PhotoRemoved ->
                _uiState.update { it.copy(photos = it.photos - action.uri) }
            is StitchFormAction.NotesChanged ->
                _uiState.update { it.copy(notes = action.value) }
            StitchFormAction.SaveStitch -> saveStitch()
            StitchFormAction.ClearError ->
                _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveStitch() {
        val current = _uiState.value
        var hasError = false

        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        if (current.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description is required") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSaving = true, nameError = null, descriptionError = null) }

        viewModelScope.launch {
            try {
                val stringListSerializer = ListSerializer(serializer<String>())

                // Parse aliases from comma-separated string
                val aliasesList = current.nameAliases
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                val aliasesJson = json.encodeToString(stringListSerializer, aliasesList)
                val photosJson = json.encodeToString(stringListSerializer, current.photos)

                val now = System.currentTimeMillis()

                if (current.isEditMode && stitchId != null) {
                    val existing = stitchRepository.getStitchById(stitchId).first()
                    if (existing != null) {
                        val updated = existing.copy(
                            name = current.name,
                            nameAliases = aliasesJson,
                            nameEs = current.nameEs.ifBlank { null },
                            abbreviation = current.abbreviation.ifBlank { null },
                            category = current.category?.name,
                            difficulty = current.difficulty?.name,
                            description = current.description,
                            hookfullyLink = current.hookfullyLink.ifBlank { null },
                            instructionLink = current.instructionLink.ifBlank { null },
                            videoLink = current.videoLink.ifBlank { null },
                            photos = photosJson,
                            notes = current.notes.ifBlank { null },
                            updatedAt = now
                        )
                        stitchRepository.updateStitch(updated)
                    }
                } else {
                    val newStitchId = stitchRepository.generateNextStitchId()
                    val entity = StitchEntity(
                        stitchId = newStitchId,
                        name = current.name,
                        nameAliases = aliasesJson,
                        nameEs = current.nameEs.ifBlank { null },
                        abbreviation = current.abbreviation.ifBlank { null },
                        category = current.category?.name,
                        difficulty = current.difficulty?.name,
                        description = current.description,
                        hookfullyLink = current.hookfullyLink.ifBlank { null },
                        instructionLink = current.instructionLink.ifBlank { null },
                        videoLink = current.videoLink.ifBlank { null },
                        photos = photosJson,
                        notes = current.notes.ifBlank { null },
                        createdAt = now,
                        updatedAt = now
                    )
                    stitchRepository.insertStitch(entity)
                }

                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
