package com.hooky.app.ui.yarns.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.hooky.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.domain.model.CareInstructions
import com.hooky.app.domain.model.enums.Material
import com.hooky.app.domain.model.enums.WeightCategory
import com.hooky.app.ui.components.BadgeStyle
import com.hooky.app.ui.components.StatusBadge
import com.hooky.app.ui.theme.BackgroundLight
import com.hooky.app.ui.theme.ErrorRed
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White
import kotlinx.serialization.json.Json

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YarnDetailScreen(
    yarnId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: YarnDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(YarnDetailAction.ClearError)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = Slate,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.yarn == null -> {
                Text(
                    stringResource(R.string.yarn_not_found),
                    modifier = Modifier.align(Alignment.Center),
                    color = TextSecondary
                )
            }
            else -> {
                YarnDetailContent(
                    yarn = uiState.yarn!!,
                    onNavigateBack = onNavigateBack,
                    onNavigateToEdit = { onNavigateToEdit(yarnId) },
                    onArchiveClick = { viewModel.onAction(YarnDetailAction.ShowArchiveDialog) }
                )
            }
        }

        // Archive dialog
        if (uiState.showArchiveDialog) {
            YarnArchiveDialog(
                onConfirm = { reason ->
                    viewModel.onAction(YarnDetailAction.ConfirmArchive(reason))
                },
                onDismiss = { viewModel.onAction(YarnDetailAction.HideArchiveDialog) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun YarnDetailContent(
    yarn: YarnEntity,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onArchiveClick: () -> Unit
) {
    val json = remember { Json { ignoreUnknownKeys = true } }

    val photos = remember(yarn.photos) {
        try { json.decodeFromString<List<String>>(yarn.photos) } catch (_: Exception) { emptyList() }
    }

    val careInstructions = remember(yarn.careInstructions) {
        try { json.decodeFromString<CareInstructions>(yarn.careInstructions) } catch (_: Exception) { CareInstructions() }
    }

    val materialDisplayName = remember(yarn.material) {
        try { Material.valueOf(yarn.material).displayName } catch (_: Exception) { yarn.material }
    }

    val weightDisplayName = remember(yarn.weightCategory) {
        yarn.weightCategory?.let {
            try { WeightCategory.valueOf(it).displayName } catch (_: Exception) { it }
        }
    }

    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Photo hero area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            if (photos.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { photos.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = photos[page],
                        contentDescription = "Photo ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Page indicator dots
                if (photos.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(photos.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) White
                                        else White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2D3748)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Workspaces,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Back button overlay
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(onClick = onNavigateBack)
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Content below photo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Yarn name
            Text(
                text = yarn.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Chips row: material + weight
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(text = materialDisplayName, style = BadgeStyle.MUTED)
                weightDisplayName?.let { weight ->
                    StatusBadge(text = weight, style = BadgeStyle.OUTLINE)
                }
            }

            // Basic Info card
            val hasBasicInfo = yarn.brand != null || yarn.colorCode != null ||
                yarn.materialComposition != null || yarn.materialSpecs != null
            if (hasBasicInfo || yarn.color.isNotBlank()) {
                YarnInfoCard(title = stringResource(R.string.yarn_section_basic)) {
                    yarn.brand?.let { InfoRow(label = stringResource(R.string.yarn_label_brand), value = it) }
                    if (yarn.color.isNotBlank()) {
                        val colorValue = if (yarn.colorCode != null) {
                            "${yarn.color} (${yarn.colorCode})"
                        } else {
                            yarn.color
                        }
                        InfoRow(label = stringResource(R.string.yarn_label_color), value = colorValue)
                    }
                    val materialValue = if (yarn.materialComposition != null) {
                        "$materialDisplayName — ${yarn.materialComposition}"
                    } else {
                        materialDisplayName
                    }
                    InfoRow(label = stringResource(R.string.yarn_label_material), value = materialValue)
                    yarn.materialSpecs?.let { InfoRow(label = stringResource(R.string.yarn_label_specs), value = it) }
                }
            }

            // Measurements card
            val hasMeasurements = yarn.ballWeightG != null || yarn.ballLengthM != null ||
                yarn.hookSizeMm != null || yarn.needleSizeMm != null || yarn.gauge != null
            if (hasMeasurements) {
                YarnInfoCard(title = stringResource(R.string.yarn_section_measurements)) {
                    yarn.ballWeightG?.let { InfoRow(label = stringResource(R.string.yarn_label_ball_weight), value = "${it}g") }
                    yarn.ballLengthM?.let { InfoRow(label = stringResource(R.string.yarn_label_ball_length), value = "${it}m") }
                    yarn.hookSizeMm?.let { InfoRow(label = stringResource(R.string.yarn_label_hook_size), value = "${it}mm") }
                    yarn.needleSizeMm?.let { InfoRow(label = stringResource(R.string.yarn_label_needle_size), value = it) }
                    yarn.gauge?.let { InfoRow(label = stringResource(R.string.yarn_label_gauge), value = it) }
                }
            }

            // Purchase card
            val hasPurchase = yarn.pricePaid != null || yarn.purchaseLocation != null
            if (hasPurchase) {
                YarnInfoCard(title = stringResource(R.string.yarn_section_purchase)) {
                    yarn.pricePaid?.let { InfoRow(label = stringResource(R.string.yarn_label_price_paid), value = "€$it") }
                    yarn.purchaseLocation?.let { InfoRow(label = stringResource(R.string.yarn_label_location), value = it) }
                    yarn.purchaseDate?.let { InfoRow(label = stringResource(R.string.yarn_label_date), value = it) }
                    yarn.purchaseLink?.let { link ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.yarn_label_link),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = link,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Slate,
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1,
                                modifier = Modifier.clickable {
                                    try { uriHandler.openUri(link) } catch (_: Exception) {}
                                }
                            )
                        }
                    }
                    yarn.quantityOwned?.let { InfoRow(label = stringResource(R.string.yarn_label_quantity_owned), value = "$it") }
                }
            }

            // Care Instructions card
            val hasCareInfo = careInstructions.machineWash || careInstructions.handWash ||
                careInstructions.dryClean || careInstructions.bleach || careInstructions.tumbleDry ||
                careInstructions.ironTemperature != null || careInstructions.notes != null
            if (hasCareInfo) {
                CareInstructionsCard(careInstructions = careInstructions)
            }

            // Notes
            yarn.notes?.let { notes ->
                YarnNotesCard(notes = notes)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate,
                        contentColor = White
                    )
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_edit))
                }

                OutlinedButton(
                    onClick = onArchiveClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text(stringResource(R.string.action_archive))
                }
            }

            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun YarnInfoCard(
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CareInstructionsCard(careInstructions: CareInstructions) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.yarn_section_care),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Care chips
            val machineWashStr = stringResource(R.string.yarn_care_machine_wash)
            val handWashStr = stringResource(R.string.yarn_care_hand_wash)
            val dryCleanStr = stringResource(R.string.yarn_care_dry_clean)
            val noBleachStr = stringResource(R.string.yarn_label_no_bleach)
            val tumbleDryStr = stringResource(R.string.yarn_care_tumble_dry)
            val careItems = buildList {
                if (careInstructions.machineWash) add(machineWashStr)
                if (careInstructions.handWash) add(handWashStr)
                if (careInstructions.dryClean) add(dryCleanStr)
                if (!careInstructions.bleach) add(noBleachStr)
                if (careInstructions.tumbleDry) add(tumbleDryStr)
            }

            if (careItems.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    careItems.forEach { item ->
                        StatusBadge(text = item, style = BadgeStyle.OUTLINE)
                    }
                }
            }

            careInstructions.ironTemperature?.let { temp ->
                InfoRow(label = stringResource(R.string.yarn_label_iron_temp), value = temp)
            }

            careInstructions.notes?.let { notes ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.yarn_label_care_notes),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun YarnNotesCard(notes: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.yarn_section_notes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun YarnArchiveDialog(
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.yarn_archive_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.yarn_archive_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.piece_archive_reason_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate,
                        cursorColor = Slate
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason.ifBlank { null }) },
                colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
            ) {
                Text(stringResource(R.string.action_archive))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
