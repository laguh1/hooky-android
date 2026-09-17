package com.hooky.app.ui.stitches.form

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.data.repository.StitchRepository
import com.hooky.app.domain.model.enums.Difficulty
import com.hooky.app.domain.model.enums.StitchCategory
import com.hooky.app.ui.util.movePhotosToStorage
import com.hooky.app.ui.util.moveSingleFileToStorage
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val chartPath: String? = null,
    val notes: String = "",
    val hasUnsavedChanges: Boolean = false,
    val nameError: String? = null
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
    data class ExtractNameFromUrl(val url: String) : StitchFormAction
    data class PhotoAdded(val uri: String) : StitchFormAction
    data class PhotoRemoved(val uri: String) : StitchFormAction
    data class PhotoReplaced(val oldPath: String, val newPath: String) : StitchFormAction
    data class ChartAdded(val path: String) : StitchFormAction
    object ChartRemoved : StitchFormAction
    data class NotesChanged(val value: String) : StitchFormAction
    object SaveStitch : StitchFormAction
    object ClearError : StitchFormAction
}

@HiltViewModel
class StitchFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
                            chartPath = stitch.chartPath,
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
        if (action !is StitchFormAction.ClearError && action !is StitchFormAction.SaveStitch) {
            _uiState.update { it.copy(hasUnsavedChanges = true) }
        }
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
                _uiState.update { it.copy(description = action.value) }
            is StitchFormAction.HookfullyLinkChanged ->
                _uiState.update { it.copy(hookfullyLink = action.value) }
            is StitchFormAction.InstructionLinkChanged ->
                _uiState.update { it.copy(instructionLink = action.value) }
            is StitchFormAction.VideoLinkChanged ->
                _uiState.update { it.copy(videoLink = action.value) }
            is StitchFormAction.ExtractNameFromUrl -> {
                if (_uiState.value.name.isBlank()) {
                    val extracted = extractNameFromUrl(action.url)
                    if (extracted != null) _uiState.update { it.copy(name = extracted, nameError = null) }
                }
            }
            is StitchFormAction.PhotoAdded ->
                _uiState.update { it.copy(photos = it.photos + action.uri) }
            is StitchFormAction.PhotoRemoved ->
                _uiState.update { it.copy(photos = it.photos - action.uri) }
            is StitchFormAction.PhotoReplaced ->
                _uiState.update { state ->
                    state.copy(photos = state.photos.map { if (it == action.oldPath) action.newPath else it })
                }
            is StitchFormAction.ChartAdded ->
                _uiState.update { it.copy(chartPath = action.path) }
            StitchFormAction.ChartRemoved ->
                _uiState.update { it.copy(chartPath = null) }
            is StitchFormAction.NotesChanged ->
                _uiState.update { it.copy(notes = action.value) }
            StitchFormAction.SaveStitch -> saveStitch()
            StitchFormAction.ClearError ->
                _uiState.update { it.copy(error = null) }
        }
    }

    private fun extractNameFromUrl(url: String): String? {
        return try {
            val uri = java.net.URI(url.trim())
            val host = uri.host?.lowercase() ?: return null

            // YouTube paths are video IDs, not stitch names
            if (host.contains("youtube") || host.contains("youtu.be")) return null

            val segments = (uri.path ?: return null)
                .split("/")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val slug = segments.lastOrNull() ?: return null

            // Skip if it looks like a random ID (no word separators, short alphanumeric)
            if (!slug.contains('-') && !slug.contains('_') && slug.length < 6) return null

            val cleaned = slug
                .substringBefore(".")           // remove file extension
                .replace(Regex("[-_]?(tutorial|pattern|how[-_]?to|crochet[-_]stitch|video)$", RegexOption.IGNORE_CASE), "")
                .replace(Regex("[_-]"), " ")
                .trim()

            if (cleaned.isBlank()) return null

            cleaned.split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
        } catch (_: Exception) { null }
    }

    private fun saveStitch() {
        val current = _uiState.value
        var hasError = false

        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSaving = true, nameError = null) }

        viewModelScope.launch {
            try {
                val stringListSerializer = ListSerializer(serializer<String>())

                // Parse aliases from comma-separated string
                val aliasesList = current.nameAliases
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                val aliasesJson = json.encodeToString(stringListSerializer, aliasesList)
                val now = System.currentTimeMillis()

                if (current.isEditMode && stitchId != null) {
                    val permanentPhotos = movePhotosToStorage(context, current.photos, "stitches", stitchId.toString())
                    val permanentChart = current.chartPath?.let { moveSingleFileToStorage(context, it, "stitches/charts", stitchId.toString()) }
                    val finalPhotosJson = json.encodeToString(stringListSerializer, permanentPhotos)
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
                            photos = finalPhotosJson,
                            chartPath = permanentChart,
                            notes = current.notes.ifBlank { null },
                            updatedAt = now
                        )
                        stitchRepository.updateStitch(updated)
                    }
                } else {
                    val newStitchId = stitchRepository.generateNextStitchId()
                    val permanentPhotos = movePhotosToStorage(context, current.photos, "stitches", newStitchId.toString())
                    val permanentChart = current.chartPath?.let { moveSingleFileToStorage(context, it, "stitches/charts", newStitchId.toString()) }
                    val finalPhotosJson = json.encodeToString(stringListSerializer, permanentPhotos)
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
                        photos = finalPhotosJson,
                        chartPath = permanentChart,
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
