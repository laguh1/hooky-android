package com.hooky.app.ui.yarns.form

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.hooky.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hooky.app.data.scanner.ScanMode
import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory
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
import com.hooky.app.ui.util.labelResId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnFormScreen(
    yarnId: Int?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit = onNavigateBack,
    onNavigateToCamera: () -> Unit = {},
    navController: NavController? = null,
    viewModel: YarnFormViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(onClick = { showUnsavedDialog = false; onNavigateBack() }) {
                    Text(stringResource(R.string.action_discard), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showUnsavedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate, contentColor = White)
                ) {
                    Text(stringResource(R.string.action_keep_editing))
                }
            }
        )
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onNavigateToCamera()
    }

    // Gallery picker — adds photo to gallery
    val galleryLauncher = rememberPhotoPickerLauncher { path ->
        viewModel.onAction(YarnFormAction.PhotoAdded(path))
    }

    // Photo editor launcher
    val editLauncher = navController?.let {
        rememberPhotoEditorLauncher(
            navController = it,
            onEditDone = { old, new -> viewModel.onAction(YarnFormAction.PhotoReplaced(old, new)) }
        )
    } ?: { _ -> }

    // Gallery picker — for label scanning
    val scanGalleryLauncher = rememberPhotoPickerLauncher { path ->
        viewModel.onAction(YarnFormAction.PhotoReceived(path))
    }

    // Helper to trigger needle scan via camera
    fun launchNeedleScan(target: ScanMode) {
        viewModel.onAction(YarnFormAction.NeedleScanRequested(target))
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) onNavigateToCamera()
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Observe photo_path result from CameraScreen — ViewModel decides: add to gallery or scan
    LaunchedEffect(navController) {
        navController?.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow("photo_path", "")
            ?.collect { path ->
                if (path.isNotBlank()) {
                    viewModel.onAction(YarnFormAction.PhotoReceived(path))
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
            viewModel.onAction(YarnFormAction.ClearError)
        }
    }

    val title = if (uiState.isEditMode) stringResource(R.string.yarn_edit) else stringResource(R.string.yarn_new)

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
                    IconButton(onClick = {
                        if (uiState.hasUnsavedChanges) showUnsavedDialog = true else onNavigateBack()
                    }) {
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
        // Label scan result dialog
        val scanResult = uiState.scanResult
        if (scanResult != null) {
            YarnLabelScanConfirmDialog(
                result = scanResult,
                onApply = { viewModel.onAction(YarnFormAction.ApplyScanResult) },
                onScanAnother = {
                    viewModel.onAction(YarnFormAction.ScanAnotherSide)
                    val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (granted) onNavigateToCamera()
                    else cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                },
                onDismiss = { viewModel.onAction(YarnFormAction.DismissScanResult) }
            )
        }

        // Needle/hook scan result dialog
        val needleScanResult = uiState.needleScanResult
        if (needleScanResult != null) {
            NeedleScanConfirmDialog(
                result = needleScanResult,
                onApply = { viewModel.onAction(YarnFormAction.ApplyNeedleScanResult) },
                onDismiss = { viewModel.onAction(YarnFormAction.DismissNeedleScanResult) }
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
            Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Scan Label — camera + gallery
                if (uiState.isScanning) {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Slate.copy(alpha = 0.5f),
                            disabledContentColor = White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        CircularProgressIndicator(
                            color = White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.yarn_scanning), style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                viewModel.onAction(YarnFormAction.ScanLabelRequested)
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) onNavigateToCamera()
                                else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Slate,
                                contentColor = White
                            ),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(
                                Icons.Filled.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.yarn_scan_label),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.onAction(YarnFormAction.ScanLabelRequested)
                                scanGalleryLauncher()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(
                                Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                stringResource(R.string.yarn_from_gallery),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.yarn_scan_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Section: Basic Info
                FormSection(title = stringResource(R.string.yarn_section_basic)) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onAction(YarnFormAction.NameChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_name) + " *") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.brand,
                        onValueChange = { viewModel.onAction(YarnFormAction.BrandChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_brand)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.color,
                        onValueChange = { viewModel.onAction(YarnFormAction.ColorChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_color_name)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Color suggestion chip — appears after photo is added
                    val suggestedColor = uiState.suggestedColor
                    if (suggestedColor != null) {
                        ColorSuggestionChip(
                            colorName = suggestedColor,
                            colorRgb = uiState.suggestedColorRgb,
                            onApply = { viewModel.onAction(YarnFormAction.ApplySuggestedColor) },
                            onDismiss = { viewModel.onAction(YarnFormAction.DismissSuggestedColor) }
                        )
                    }

                    OutlinedTextField(
                        value = uiState.colorCode,
                        onValueChange = { viewModel.onAction(YarnFormAction.ColorCodeChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_color_code)) },
                        placeholder = { Text(stringResource(R.string.yarn_field_color_code_hint), color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Photos — early so label scan color suggestion appears right away
                FormSection(title = stringResource(R.string.yarn_section_photos)) {
                    PhotoGallery(
                        photos = uiState.photos,
                        height = 220.dp,
                        onDeletePhoto = { path ->
                            viewModel.onAction(YarnFormAction.PhotoRemoved(path))
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

                // Section: Material
                FormSection(title = stringResource(R.string.yarn_section_material)) {
                    val materialLabels = Material.values().map { stringResource(it.labelResId) }
                    EnumDropdown(
                        label = stringResource(R.string.yarn_field_material),
                        selected = stringResource(uiState.material.labelResId),
                        options = materialLabels,
                        onSelect = { display ->
                            val idx = materialLabels.indexOf(display)
                            val mat = if (idx >= 0) Material.values()[idx] else Material.values().first()
                            viewModel.onAction(YarnFormAction.MaterialChanged(mat))
                        }
                    )

                    OutlinedTextField(
                        value = uiState.materialComposition,
                        onValueChange = { viewModel.onAction(YarnFormAction.MaterialCompositionChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_composition)) },
                        placeholder = { Text(stringResource(R.string.yarn_field_composition_hint), color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.materialSpecs,
                        onValueChange = { viewModel.onAction(YarnFormAction.MaterialSpecsChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_specs)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Weight category dropdown — nullable, with "Not specified" option
                    val notSpecifiedStr = stringResource(R.string.not_specified)
                    val weightLabels = WeightCategory.values().map { stringResource(it.labelResId) }
                    val weightOptions = listOf(notSpecifiedStr) + weightLabels
                    val selectedWeight = uiState.weightCategory?.let { stringResource(it.labelResId) } ?: notSpecifiedStr
                    EnumDropdown(
                        label = stringResource(R.string.yarn_field_weight_category),
                        selected = selectedWeight,
                        options = weightOptions,
                        onSelect = { display ->
                            val wc = if (display == notSpecifiedStr) null
                            else {
                                val idx = weightLabels.indexOf(display)
                                if (idx >= 0) WeightCategory.values()[idx] else null
                            }
                            viewModel.onAction(YarnFormAction.WeightCategoryChanged(wc))
                        }
                    )
                }

                // Section: Ball Info
                FormSection(title = stringResource(R.string.yarn_section_ball_info)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.ballWeightG,
                            onValueChange = { viewModel.onAction(YarnFormAction.BallWeightGChanged(it)) },
                            label = { Text(stringResource(R.string.yarn_field_ball_weight)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = formTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = uiState.ballLengthM,
                            onValueChange = { viewModel.onAction(YarnFormAction.BallLengthMChanged(it)) },
                            label = { Text(stringResource(R.string.yarn_field_ball_length)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(10.dp),
                            colors = formTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = uiState.hookSizeMm,
                        onValueChange = { viewModel.onAction(YarnFormAction.HookSizeMmChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_hook_size)) },
                        trailingIcon = {
                            IconButton(onClick = { launchNeedleScan(ScanMode.HOOK) }) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = stringResource(R.string.yarn_label_hook_size), modifier = Modifier.size(20.dp), tint = TextSecondary)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.needleSizeMm,
                        onValueChange = { viewModel.onAction(YarnFormAction.NeedleSizeMmChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_needle_size)) },
                        placeholder = { Text(stringResource(R.string.yarn_field_needle_size_hint), color = TextMuted) },
                        trailingIcon = {
                            IconButton(onClick = { launchNeedleScan(ScanMode.NEEDLE) }) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = stringResource(R.string.yarn_label_needle_size), modifier = Modifier.size(20.dp), tint = TextSecondary)
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.gauge,
                        onValueChange = { viewModel.onAction(YarnFormAction.GaugeChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_gauge)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Purchase
                FormSection(title = stringResource(R.string.yarn_section_purchase)) {
                    OutlinedTextField(
                        value = uiState.pricePaid,
                        onValueChange = { viewModel.onAction(YarnFormAction.PricePaidChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_price_paid)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.purchaseLocation,
                        onValueChange = { viewModel.onAction(YarnFormAction.PurchaseLocationChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_purchase_location)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    com.hooky.app.ui.pieces.form.DatePickerField(
                        label = stringResource(R.string.yarn_field_purchase_date),
                        value = uiState.purchaseDate,
                        onValueChange = { viewModel.onAction(YarnFormAction.PurchaseDateChanged(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.purchaseLink,
                        onValueChange = { viewModel.onAction(YarnFormAction.PurchaseLinkChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_purchase_link)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.quantityOwned,
                        onValueChange = { viewModel.onAction(YarnFormAction.QuantityOwnedChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_field_quantity)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Care Instructions
                FormSection(title = stringResource(R.string.yarn_section_care)) {
                    CareCheckboxRow(
                        label = stringResource(R.string.yarn_care_machine_wash),
                        checked = uiState.machineWash,
                        onCheckedChange = { viewModel.onAction(YarnFormAction.MachineWashChanged(it)) }
                    )
                    CareCheckboxRow(
                        label = stringResource(R.string.yarn_care_hand_wash),
                        checked = uiState.handWash,
                        onCheckedChange = { viewModel.onAction(YarnFormAction.HandWashChanged(it)) }
                    )
                    CareCheckboxRow(
                        label = stringResource(R.string.yarn_care_dry_clean),
                        checked = uiState.dryClean,
                        onCheckedChange = { viewModel.onAction(YarnFormAction.DryCleanChanged(it)) }
                    )
                    CareCheckboxRow(
                        label = stringResource(R.string.yarn_care_bleach),
                        checked = uiState.bleach,
                        onCheckedChange = { viewModel.onAction(YarnFormAction.BleachChanged(it)) }
                    )
                    CareCheckboxRow(
                        label = stringResource(R.string.yarn_care_tumble_dry),
                        checked = uiState.tumbleDry,
                        onCheckedChange = { viewModel.onAction(YarnFormAction.TumbleDryChanged(it)) }
                    )

                    WashTemperatureDropdown(
                        selected = uiState.washTemperature,
                        onSelected = { viewModel.onAction(YarnFormAction.WashTemperatureChanged(it)) }
                    )

                    OutlinedTextField(
                        value = uiState.careNotes,
                        onValueChange = { viewModel.onAction(YarnFormAction.CareNotesChanged(it)) },
                        label = { Text(stringResource(R.string.yarn_label_care_notes)) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Section: Notes
                FormSection(title = stringResource(R.string.yarn_section_notes)) {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onAction(YarnFormAction.NotesChanged(it)) },
                        label = { Text(stringResource(R.string.label_notes)) },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = formTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Save button — always Slate, regardless of create/edit mode, so the
                // primary action stays visually consistent across the app
                val saveColor = Slate
                Button(
                    onClick = { viewModel.onAction(YarnFormAction.SaveYarn) },
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = saveColor,
                        contentColor = White,
                        disabledContainerColor = saveColor.copy(alpha = 0.5f),
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
                            text = if (uiState.isEditMode) stringResource(R.string.action_save_changes) else stringResource(R.string.yarn_add),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
            }
            } // end Box
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

@Composable
private fun CareCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Slate,
                uncheckedColor = androidx.compose.ui.graphics.Color(0xFF9CA3AF)
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
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

@Composable
private fun ColorSuggestionChip(
    colorName: String,
    colorRgb: Int?,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Filled.Palette,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextMuted
        )
        Text(
            text = stringResource(R.string.yarn_color_detected),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        if (colorRgb != null) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = Color(colorRgb),
                        shape = CircleShape
                    )
                    .border(0.5.dp, BorderLight, CircleShape)
            )
        }
        Text(
            text = colorName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onApply, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.yarn_color_apply), modifier = Modifier.size(16.dp), tint = Slate)
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.yarn_color_dismiss), modifier = Modifier.size(16.dp), tint = TextMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WashTemperatureDropdown(selected: String, onSelected: (String) -> Unit) {
    val options = listOf(
        "",
        stringResource(R.string.yarn_wash_temp_cold),
        stringResource(R.string.yarn_wash_temp_warm),
        stringResource(R.string.yarn_wash_temp_hot),
        stringResource(R.string.yarn_wash_temp_very_hot),
        stringResource(R.string.yarn_wash_temp_do_not_wash)
    )
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.yarn_care_iron_temp)) },
            placeholder = { Text(stringResource(R.string.yarn_wash_temp_select), color = TextMuted) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = formTextFieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.drop(1).forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelected(option); expanded = false }
                )
            }
            if (selected.isNotBlank()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.yarn_wash_temp_clear), color = TextMuted) },
                    onClick = { onSelected(""); expanded = false }
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
