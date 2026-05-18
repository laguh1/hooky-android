package com.hooky.app.ui.stitches.form

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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.hooky.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hooky.app.domain.model.enums.Difficulty
import com.hooky.app.domain.model.enums.StitchCategory
import com.hooky.app.ui.camera.rememberPhotoEditorLauncher
import com.hooky.app.ui.camera.rememberPhotoPickerLauncher
import com.hooky.app.ui.components.PhotoGallery
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.BrandPurple
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White

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

    // Photo editor launcher
    val editLauncher = rememberPhotoEditorLauncher(
        onEditDone = { old, new -> viewModel.onAction(StitchFormAction.PhotoReplaced(old, new)) },
        onNoEditor = { android.widget.Toast.makeText(context, "No photo editor found", android.widget.Toast.LENGTH_SHORT).show() }
    )

    // Chart image picker (gallery)
    val chartImageLauncher = rememberPhotoPickerLauncher { path ->
        viewModel.onAction(StitchFormAction.ChartAdded(path))
    }

    // Chart PDF picker
    val chartScope = rememberCoroutineScope()
    val chartPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        chartScope.launch {
            val dest = withContext(Dispatchers.IO) {
                try {
                    val dir = File(context.filesDir, "stitches/charts").also { it.mkdirs() }
                    val file = File(dir, "chart_${System.currentTimeMillis()}.pdf")
                    context.contentResolver.openInputStream(uri)?.use { it.copyTo(file.outputStream()) }
                    if (file.exists() && file.length() > 0) file.absolutePath else null
                } catch (_: Exception) { null }
            }
            if (dest != null) viewModel.onAction(StitchFormAction.ChartAdded(dest))
        }
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

    val title = if (uiState.isEditMode) stringResource(R.string.stitch_edit) else stringResource(R.string.stitch_new)

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                // Section: Tutorial Links — first so name auto-fills from URL
                FormSection(title = stringResource(R.string.stitch_section_links)) {
                    OutlinedTextField(
                        value = uiState.instructionLink,
                        onValueChange = { viewModel.onAction(StitchFormAction.InstructionLinkChanged(it)) },
                        label = { Text(stringResource(R.string.stitch_field_instruction_link)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { fs ->
                                if (!fs.isFocused && uiState.instructionLink.isNotBlank())
                                    viewModel.onAction(StitchFormAction.ExtractNameFromUrl(uiState.instructionLink))
                            }
                    )

                    OutlinedTextField(
                        value = uiState.videoLink,
                        onValueChange = { viewModel.onAction(StitchFormAction.VideoLinkChanged(it)) },
                        label = { Text(stringResource(R.string.stitch_field_video_link)) },
                        placeholder = { Text(stringResource(R.string.stitch_video_link_hint), color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { fs ->
                                if (!fs.isFocused && uiState.videoLink.isNotBlank())
                                    viewModel.onAction(StitchFormAction.ExtractNameFromUrl(uiState.videoLink))
                            }
                    )
                }

                // Section: Basic Info
                FormSection(title = stringResource(R.string.stitch_section_basic)) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onAction(StitchFormAction.NameChanged(it)) },
                        label = { Text(stringResource(R.string.stitch_field_name) + " *") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.nameAliases,
                        onValueChange = { viewModel.onAction(StitchFormAction.NameAliasesChanged(it)) },
                        label = { Text(stringResource(R.string.stitch_field_aliases)) },
                        placeholder = { Text(stringResource(R.string.stitch_aliases_hint), color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Photos
                FormSection(title = stringResource(R.string.stitch_section_photos)) {
                    PhotoGallery(
                        photos = uiState.photos,
                        height = 220.dp,
                        onDeletePhoto = { path ->
                            viewModel.onAction(StitchFormAction.PhotoRemoved(path))
                        },
                        onEditPhoto = { path -> editLauncher(path) },
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
                            Text(stringResource(R.string.action_take_photo))
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
                            Text(stringResource(R.string.action_gallery))
                        }
                    }
                }

                // Section: Classification
                FormSection(title = stringResource(R.string.stitch_section_classification)) {
                    val categoryOptions = listOf(stringResource(R.string.not_specified)) + StitchCategory.values().map { it.displayName }
                    val selectedCategory = uiState.category?.displayName ?: stringResource(R.string.not_specified)
                    EnumDropdown(
                        label = stringResource(R.string.stitch_field_category),
                        selected = selectedCategory,
                        options = categoryOptions,
                        onSelect = { display ->
                            val notSpecified = categoryOptions.first()
                            val cat = if (display == notSpecified) null
                            else StitchCategory.values().firstOrNull { it.displayName == display }
                            viewModel.onAction(StitchFormAction.CategoryChanged(cat))
                        }
                    )

                    val difficultyOptions = listOf(stringResource(R.string.not_specified)) + Difficulty.values().map { it.displayName }
                    val selectedDifficulty = uiState.difficulty?.displayName ?: stringResource(R.string.not_specified)
                    EnumDropdown(
                        label = stringResource(R.string.stitch_field_difficulty),
                        selected = selectedDifficulty,
                        options = difficultyOptions,
                        onSelect = { display ->
                            val notSpecified = difficultyOptions.first()
                            val diff = if (display == notSpecified) null
                            else Difficulty.values().firstOrNull { it.displayName == display }
                            viewModel.onAction(StitchFormAction.DifficultyChanged(diff))
                        }
                    )
                }

                // Section: Description
                FormSection(title = stringResource(R.string.stitch_section_description)) {
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onAction(StitchFormAction.DescriptionChanged(it)) },
                        label = { Text(stringResource(R.string.stitch_field_description) + " *") },
                        isError = uiState.descriptionError != null,
                        supportingText = uiState.descriptionError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Stitch Chart
                FormSection(title = stringResource(R.string.stitch_section_chart)) {
                    val chartPath = uiState.chartPath
                    if (chartPath != null) {
                        if (chartPath.endsWith(".pdf", ignoreCase = true)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(32.dp), tint = BrandPurple)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = File(chartPath).name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2
                                )
                                IconButton(onClick = { viewModel.onAction(StitchFormAction.ChartRemoved) }) {
                                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.stitch_chart_remove), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = File(chartPath),
                                    contentDescription = "Stitch chart",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { viewModel.onAction(StitchFormAction.ChartRemoved) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.stitch_chart_remove), tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.stitch_chart_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { chartImageLauncher() },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.action_gallery))
                        }

                        OutlinedButton(
                            onClick = { chartPdfLauncher.launch("application/pdf") },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.action_upload_pdf))
                        }
                    }
                }

                // Section: Notes
                FormSection(title = stringResource(R.string.stitch_section_notes)) {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onAction(StitchFormAction.NotesChanged(it)) },
                        label = { Text(stringResource(R.string.label_notes)) },
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
                            text = if (uiState.isEditMode) stringResource(R.string.action_save_changes) else stringResource(R.string.stitch_create),
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
    focusedBorderColor = BrandPurple,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    cursorColor = BrandPurple,
    focusedLabelColor = BrandPurple,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
