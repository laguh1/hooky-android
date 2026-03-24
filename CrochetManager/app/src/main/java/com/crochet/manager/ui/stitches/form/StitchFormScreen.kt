package com.crochet.manager.ui.stitches.form

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.crochet.manager.domain.model.enums.Difficulty
import com.crochet.manager.domain.model.enums.StitchCategory
import com.crochet.manager.ui.camera.rememberPhotoPickerLauncher
import com.crochet.manager.ui.components.PhotoGallery
import com.crochet.manager.ui.theme.BorderLight
import com.crochet.manager.ui.theme.Slate
import com.crochet.manager.ui.theme.TextMuted
import com.crochet.manager.ui.theme.TextSecondary
import com.crochet.manager.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchFormScreen(
    stitchId: Int?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit = onNavigateBack,
    onNavigateToCamera: () -> Unit = {},
    navController: NavController? = null,
    viewModel: StitchFormViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onNavigateToCamera()
    }

    // Gallery picker
    val galleryLauncher = rememberPhotoPickerLauncher { path ->
        viewModel.onAction(StitchFormAction.PhotoAdded(path))
    }

    // Observe photo_path result from CameraScreen
    LaunchedEffect(navController) {
        navController?.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow("photo_path", "")
            ?.collect { path ->
                if (path.isNotBlank()) {
                    viewModel.onAction(StitchFormAction.PhotoAdded(path))
                    navController.currentBackStackEntry?.savedStateHandle?.set("photo_path", "")
                }
            }
    }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onSaved()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(StitchFormAction.ClearError)
        }
    }

    val title = if (uiState.isEditMode) "Edit Stitch" else "New Stitch"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Slate)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Basic Info
                FormSection(title = "Basic Info") {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onAction(StitchFormAction.NameChanged(it)) },
                        label = { Text("Name *") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.nameEs,
                        onValueChange = { viewModel.onAction(StitchFormAction.NameEsChanged(it)) },
                        label = { Text("Spanish name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.abbreviation,
                        onValueChange = { viewModel.onAction(StitchFormAction.AbbreviationChanged(it)) },
                        label = { Text("Abbreviation") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.nameAliases,
                        onValueChange = { viewModel.onAction(StitchFormAction.NameAliasesChanged(it)) },
                        label = { Text("Aliases") },
                        placeholder = { Text("e.g. dc, double, dbl cr", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Classification
                FormSection(title = "Classification") {
                    val categoryOptions = listOf("Not specified") + StitchCategory.values().map { it.displayName }
                    val selectedCategory = uiState.category?.displayName ?: "Not specified"
                    EnumDropdown(
                        label = "Category",
                        selected = selectedCategory,
                        options = categoryOptions,
                        onSelect = { display ->
                            val cat = if (display == "Not specified") null
                            else StitchCategory.values().firstOrNull { it.displayName == display }
                            viewModel.onAction(StitchFormAction.CategoryChanged(cat))
                        }
                    )

                    val difficultyOptions = listOf("Not specified") + Difficulty.values().map { it.displayName }
                    val selectedDifficulty = uiState.difficulty?.displayName ?: "Not specified"
                    EnumDropdown(
                        label = "Difficulty",
                        selected = selectedDifficulty,
                        options = difficultyOptions,
                        onSelect = { display ->
                            val diff = if (display == "Not specified") null
                            else Difficulty.values().firstOrNull { it.displayName == display }
                            viewModel.onAction(StitchFormAction.DifficultyChanged(diff))
                        }
                    )
                }

                // Section: Description
                FormSection(title = "Description") {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onAction(StitchFormAction.DescriptionChanged(it)) },
                        label = { Text("Description *") },
                        isError = uiState.descriptionError != null,
                        supportingText = uiState.descriptionError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Tutorial Links
                FormSection(title = "Tutorial Links") {
                    OutlinedTextField(
                        value = uiState.hookfullyLink,
                        onValueChange = { viewModel.onAction(StitchFormAction.HookfullyLinkChanged(it)) },
                        label = { Text("Hookfully link") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.instructionLink,
                        onValueChange = { viewModel.onAction(StitchFormAction.InstructionLinkChanged(it)) },
                        label = { Text("Instruction page link") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.videoLink,
                        onValueChange = { viewModel.onAction(StitchFormAction.VideoLinkChanged(it)) },
                        label = { Text("Video tutorial link") },
                        placeholder = { Text("YouTube URL", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Photos
                FormSection(title = "Photos") {
                    PhotoGallery(
                        photos = uiState.photos,
                        height = 220.dp,
                        onDeletePhoto = { path ->
                            viewModel.onAction(StitchFormAction.PhotoRemoved(path))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    onNavigateToCamera()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Photo")
                        }

                        OutlinedButton(
                            onClick = { galleryLauncher() },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery")
                        }
                    }
                }

                // Section: Notes
                FormSection(title = "Notes") {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onAction(StitchFormAction.NotesChanged(it)) },
                        label = { Text("Notes") },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Save button
                Button(
                    onClick = { viewModel.onAction(StitchFormAction.SaveStitch) },
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate,
                        contentColor = White,
                        disabledContainerColor = Slate.copy(alpha = 0.5f),
                        disabledContentColor = White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isEditMode) "Save Changes" else "Create Stitch",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(10.dp),
            colors = formTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun formTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Slate,
    unfocusedBorderColor = BorderLight,
    cursorColor = Slate,
    focusedLabelColor = Slate,
    unfocusedLabelColor = TextSecondary
)
