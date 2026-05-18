package com.hooky.app.ui.pieces.form

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hooky.app.R
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hooky.app.domain.model.enums.Destination
import com.hooky.app.domain.model.enums.PieceType
import com.hooky.app.domain.model.enums.WorkStatus
import com.hooky.app.ui.camera.rememberPhotoEditorLauncher
import com.hooky.app.ui.camera.rememberPhotoPickerLauncher
import com.hooky.app.ui.components.NeedleScanConfirmDialog
import com.hooky.app.ui.components.PhotoGallery
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PieceFormScreen(
    pieceId: Int?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit = onNavigateBack,
    onNavigateToCamera: () -> Unit = {},
    navController: NavController? = null,
    viewModel: PieceFormViewModel = hiltViewModel()
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

    // Gallery picker launcher
    val galleryLauncher = rememberPhotoPickerLauncher { path ->
        viewModel.onAction(PieceFormAction.PhotoAdded(path))
    }

    // Photo editor launcher
    val editLauncher = rememberPhotoEditorLauncher(
        onEditDone = { old, new -> viewModel.onAction(PieceFormAction.PhotoReplaced(old, new)) },
        onNoEditor = { android.widget.Toast.makeText(context, "No photo editor found", android.widget.Toast.LENGTH_SHORT).show() }
    )

    // Observe photo_path result from CameraScreen — ViewModel decides: add to gallery or scan
    LaunchedEffect(navController) {
        navController?.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow("photo_path", "")
            ?.collect { path ->
                if (path.isNotBlank()) {
                    viewModel.onAction(PieceFormAction.PhotoReceived(path))
                    navController.currentBackStackEntry?.savedStateHandle?.set("photo_path", "")
                }
            }
    }

    // Navigate back on save
    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onSaved()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(PieceFormAction.ClearError)
        }
    }

    val title = if (uiState.isEditMode) stringResource(R.string.piece_edit) else stringResource(R.string.piece_new)

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
        // Hook scan result dialog
        val needleScanResult = uiState.needleScanResult
        if (needleScanResult != null) {
            NeedleScanConfirmDialog(
                result = needleScanResult,
                onApply = { viewModel.onAction(PieceFormAction.ApplyNeedleScanResult) },
                onDismiss = { viewModel.onAction(PieceFormAction.DismissNeedleScanResult) }
            )
        }

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
                FormSection(title = stringResource(R.string.yarn_section_basic)) {
                    // Name
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onAction(PieceFormAction.NameChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_name) + " *") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type dropdown
                    EnumDropdown(
                        label = stringResource(R.string.piece_field_type),
                        selected = uiState.type.displayName,
                        options = PieceType.values().map { it.displayName },
                        onSelect = { display ->
                            val pieceType = PieceType.values().first { it.displayName == display }
                            viewModel.onAction(PieceFormAction.TypeChanged(pieceType))
                        }
                    )

                    // Work Status dropdown
                    EnumDropdown(
                        label = stringResource(R.string.piece_field_status),
                        selected = uiState.workStatus.displayName,
                        options = WorkStatus.values().map { it.displayName },
                        onSelect = { display ->
                            val ws = WorkStatus.values().first { it.displayName == display }
                            viewModel.onAction(PieceFormAction.WorkStatusChanged(ws))
                        }
                    )

                    // Destination dropdown
                    EnumDropdown(
                        label = stringResource(R.string.piece_field_destination),
                        selected = uiState.destination.displayName,
                        options = Destination.values().map { it.displayName },
                        onSelect = { display ->
                            val dest = Destination.values().first { it.displayName == display }
                            viewModel.onAction(PieceFormAction.DestinationChanged(dest))
                        }
                    )
                }

                // Section: Photos — early so color detection runs before filling details
                FormSection(title = stringResource(R.string.label_photos)) {
                    PhotoGallery(
                        photos = uiState.photos,
                        height = 220.dp,
                        onDeletePhoto = { path ->
                            viewModel.onAction(PieceFormAction.PhotoRemoved(path))
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
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
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
                            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_gallery))
                        }
                    }
                }

                // Section: Dimensions
                FormSection(title = stringResource(R.string.label_dimensions)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.widthCm,
                            onValueChange = { viewModel.onAction(PieceFormAction.WidthCmChanged(it)) },
                            label = { Text(stringResource(R.string.piece_field_width)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = formTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uiState.lengthCm,
                            onValueChange = { viewModel.onAction(PieceFormAction.LengthCmChanged(it)) },
                            label = { Text(stringResource(R.string.piece_field_length)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = formTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section: Dates & Hours
                FormSection(title = stringResource(R.string.piece_section_dates_hours)) {
                    OutlinedTextField(
                        value = uiState.dateStarted,
                        onValueChange = { viewModel.onAction(PieceFormAction.DateStartedChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_date_started)) },
                        placeholder = { Text(stringResource(R.string.label_date_hint), color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.dateFinished,
                        onValueChange = { viewModel.onAction(PieceFormAction.DateFinishedChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_date_finished)) },
                        placeholder = { Text(stringResource(R.string.label_date_hint), color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.workHours,
                        onValueChange = { viewModel.onAction(PieceFormAction.WorkHoursChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_work_hours)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.rowCount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.onAction(PieceFormAction.RowCountChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_row_start)) },
                        placeholder = { Text(stringResource(R.string.piece_field_row_start_hint), color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.targetRowCount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.onAction(PieceFormAction.TargetRowCountChanged(it)) },
                        label = { Text(stringResource(R.string.piece_row_target)) },
                        placeholder = { Text(stringResource(R.string.piece_field_target_rows_hint), color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Materials & Stitches
                FormSection(title = stringResource(R.string.piece_section_materials_stitches)) {
                    LibraryMultiPicker(
                        addLabel = stringResource(R.string.piece_picker_add_yarn),
                        dialogTitle = stringResource(R.string.piece_picker_select_yarn),
                        emptyText = stringResource(R.string.piece_picker_empty_yarn),
                        selectedIds = uiState.yarnsUsed,
                        items = uiState.availableYarns.map { yarn ->
                            yarn.yarnId to "${yarn.name}${yarn.brand?.let { b -> " – $b" } ?: ""}"
                        },
                        onSelectionChanged = { viewModel.onAction(PieceFormAction.YarnsUsedChanged(it)) }
                    )
                    LibraryMultiPicker(
                        addLabel = stringResource(R.string.piece_picker_add_needle),
                        dialogTitle = stringResource(R.string.piece_picker_select_needle),
                        emptyText = stringResource(R.string.piece_picker_empty_needle),
                        selectedIds = uiState.needlesUsed,
                        items = uiState.availableNeedles.map { needle ->
                            needle.needleId to "${needle.name}${needle.sizeMm?.let { s -> " ${s}mm" } ?: ""}"
                        },
                        onSelectionChanged = { viewModel.onAction(PieceFormAction.NeedlesUsedChanged(it)) }
                    )
                    LibraryMultiPicker(
                        addLabel = stringResource(R.string.piece_picker_add_stitch),
                        dialogTitle = stringResource(R.string.piece_picker_select_stitch),
                        emptyText = stringResource(R.string.piece_picker_empty_stitch),
                        selectedIds = uiState.stitchesUsed,
                        items = uiState.availableStitches.map { stitch ->
                            stitch.stitchId to stitch.name
                        },
                        onSelectionChanged = { viewModel.onAction(PieceFormAction.StitchesUsedChanged(it)) }
                    )
                }

                // Section: Pricing — only for sale/sold pieces
                if (uiState.destination == Destination.FOR_SALE || uiState.destination == Destination.SOLD) FormSection(title = stringResource(R.string.piece_section_pricing)) {
                    OutlinedTextField(
                        value = uiState.materialCost,
                        onValueChange = { viewModel.onAction(PieceFormAction.MaterialCostChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_material_cost)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.onAction(PieceFormAction.PriceChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_price)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.salePlatform,
                        onValueChange = { viewModel.onAction(PieceFormAction.SalePlatformChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_sale_platform)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.saleLink,
                        onValueChange = { viewModel.onAction(PieceFormAction.SaleLinkChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_sale_link)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.soldDate,
                        onValueChange = { viewModel.onAction(PieceFormAction.SoldDateChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_sold_date)) },
                        placeholder = { Text(stringResource(R.string.label_date_hint), color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.soldPrice,
                        onValueChange = { viewModel.onAction(PieceFormAction.SoldPriceChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_sold_price)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Gift — only for gift/gifted pieces
                if (uiState.destination == Destination.FOR_GIFT || uiState.destination == Destination.GIFTED) FormSection(title = stringResource(R.string.piece_section_gift)) {
                    OutlinedTextField(
                        value = uiState.giftRecipient,
                        onValueChange = { viewModel.onAction(PieceFormAction.GiftRecipientChanged(it)) },
                        label = { Text(stringResource(R.string.piece_field_gift_recipient_name)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Notes
                FormSection(title = stringResource(R.string.label_notes)) {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onAction(PieceFormAction.NotesChanged(it)) },
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
                    onClick = { viewModel.onAction(PieceFormAction.SavePiece) },
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
                            text = if (uiState.isEditMode) stringResource(R.string.action_save_changes) else stringResource(R.string.piece_create),
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LibraryMultiPicker(
    addLabel: String,
    dialogTitle: String,
    emptyText: String,
    selectedIds: List<String>,
    items: List<Pair<String, String>>,
    onSelectionChanged: (List<String>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (selectedIds.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedIds.forEach { id ->
                    val displayLabel = items.firstOrNull { it.first == id }?.second ?: id
                    FilterChip(
                        selected = true,
                        onClick = {
                            onSelectionChanged(selectedIds - id)
                        },
                        label = { Text(displayLabel, style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.action_remove_item, displayLabel),
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            selectedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            selectedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }
        }
        OutlinedButton(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(addLabel)
        }
    }

    if (showDialog) {
        var tempSelected by remember { mutableStateOf(selectedIds.toSet()) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    dialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items.forEach { (id, displayLabel) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = id in tempSelected,
                                onCheckedChange = { checked ->
                                    tempSelected = if (checked) tempSelected + id else tempSelected - id
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (items.isEmpty()) {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectionChanged(tempSelected.toList())
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate)
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = TextSecondary)
                }
            }
        )
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
