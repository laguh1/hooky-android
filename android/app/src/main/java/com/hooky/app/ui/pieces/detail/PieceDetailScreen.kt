package com.hooky.app.ui.pieces.detail

import com.hooky.app.ui.components.WordSafeText
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.hooky.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.hooky.app.data.db.entity.CounterEntity
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.domain.model.WorkSession
import com.hooky.app.domain.model.enums.Destination
import com.hooky.app.domain.model.enums.PieceType
import com.hooky.app.domain.model.enums.WorkStatus
import com.hooky.app.ui.components.BadgeStyle
import com.hooky.app.ui.components.StatusBadge
import com.hooky.app.ui.share.ShareCardGenerator
import com.hooky.app.ui.theme.BackgroundLight
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.BorderStrong
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.ErrorRed
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White
import com.hooky.app.ui.settings.formatCurrency
import com.hooky.app.ui.settings.getCurrencySymbol
import com.hooky.app.ui.util.labelResId
import com.hooky.app.util.formatWorkTime
import com.hooky.app.util.toDisplayDate

private fun formatDate(iso: String): String = iso.toDisplayDate()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PieceDetailScreen(
    pieceId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: PieceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(PieceDetailAction.ClearError)
        }
    }

    LaunchedEffect(uiState.navigateToClone) {
        uiState.navigateToClone?.let { newId ->
            viewModel.onAction(PieceDetailAction.ClearCloneNavigation)
            onNavigateToEdit(newId)
        }
    }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = Slate,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.piece == null -> {
                Text(
                    stringResource(R.string.piece_not_found),
                    modifier = Modifier.align(Alignment.Center),
                    color = TextSecondary
                )
            }
            else -> {
                PieceDetailContent(
                    piece = uiState.piece!!,
                    timerDisplaySeconds = uiState.timerDisplaySeconds,
                    timerState = uiState.timerState,
                    yarnNames = uiState.yarnNames,
                    stitchNames = uiState.stitchNames,
                    needleNames = uiState.needleNames,
                    isPremium = uiState.isPremium,
                    counters = uiState.counters,
                    onNavigateBack = onNavigateBack,
                    onNavigateToEdit = { onNavigateToEdit(pieceId) },
                    onCloneClick = { viewModel.onAction(PieceDetailAction.ClonePiece) },
                    onArchiveClick = { viewModel.onAction(PieceDetailAction.ShowArchiveDialog) },
                    onIncrementRow = { viewModel.onAction(PieceDetailAction.IncrementRow) },
                    onDecrementRow = { viewModel.onAction(PieceDetailAction.DecrementRow) },
                    onIncrementRowBy = { viewModel.onAction(PieceDetailAction.IncrementRowBy(it)) },
                    onSetRowCount = { count, target -> viewModel.onAction(PieceDetailAction.SetRowCount(count, target)) },
                    onShowSnackbar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                    onStartTimer = { viewModel.onAction(PieceDetailAction.StartTimer) },
                    onPauseTimer = { viewModel.onAction(PieceDetailAction.PauseTimer) },
                    onResumeTimer = { viewModel.onAction(PieceDetailAction.ResumeTimer) },
                    onStopTimer = { viewModel.onAction(PieceDetailAction.StopTimer) },
                    onEditWorkTime = { seconds -> viewModel.onAction(PieceDetailAction.SetWorkTime(seconds)) },
                    onApplySuggestedPrice = { price -> viewModel.onAction(PieceDetailAction.ApplySuggestedPrice(price)) },
                    onAddCounter = { name, target -> viewModel.onAction(PieceDetailAction.AddCounter(name, target)) },
                    onDeleteCounter = { id -> viewModel.onAction(PieceDetailAction.DeleteCounter(id)) },
                    onIncrementCounter = { id -> viewModel.onAction(PieceDetailAction.IncrementCounter(id)) },
                    onDecrementCounter = { id -> viewModel.onAction(PieceDetailAction.DecrementCounter(id)) },
                    onResetCounter = { id -> viewModel.onAction(PieceDetailAction.ResetCounter(id)) },
                    onUpdateCounter = { counter -> viewModel.onAction(PieceDetailAction.UpdateCounter(counter)) },
                )
            }
        }

        // Archive dialog
        if (uiState.showArchiveDialog) {
            ArchiveDialog(
                onConfirm = { reason ->
                    viewModel.onAction(PieceDetailAction.ConfirmArchive(reason))
                },
                onDismiss = { viewModel.onAction(PieceDetailAction.HideArchiveDialog) }
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
private fun PieceDetailContent(
    piece: PieceEntity,
    timerDisplaySeconds: Long,
    timerState: TimerState,
    yarnNames: Map<String, String>,
    stitchNames: Map<String, String>,
    needleNames: Map<String, String>,
    isPremium: Boolean,
    counters: List<CounterEntity>,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onCloneClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onIncrementRow: () -> Unit,
    onDecrementRow: () -> Unit,
    onIncrementRowBy: (Int) -> Unit,
    onSetRowCount: (Int, Int?) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onEditWorkTime: (Long) -> Unit,
    onApplySuggestedPrice: (Float) -> Unit,
    onAddCounter: (String, Int?) -> Unit,
    onDeleteCounter: (Int) -> Unit,
    onIncrementCounter: (Int) -> Unit,
    onDecrementCounter: (Int) -> Unit,
    onResetCounter: (Int) -> Unit,
    onUpdateCounter: (CounterEntity) -> Unit,
) {
    var showPriceSuggestion by remember { mutableStateOf(false) }
    var isSharing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val shareScope = rememberCoroutineScope()

    val photos = remember(piece.photos) {
        try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.photos)
        } catch (_: Exception) { emptyList() }
    }
    val workSessions = remember(piece.workSessions) {
        try {
            kotlinx.serialization.json.Json.decodeFromString<List<WorkSession>>(piece.workSessions)
        } catch (_: Exception) { emptyList() }
    }
    val yarnsUsed = remember(piece.yarnsUsed) {
        try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.yarnsUsed)
        } catch (_: Exception) { emptyList() }
    }
    val stitchesUsed = remember(piece.stitchesUsed) {
        try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.stitchesUsed)
        } catch (_: Exception) { emptyList() }
    }
    val needlesUsed = remember(piece.needlesUsed) {
        try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(piece.needlesUsed)
        } catch (_: Exception) { emptyList() }
    }

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
                // Placeholder with subtle grid pattern look
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2D3748)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Texture,
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
            // Piece name
            Text(
                text = piece.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Status chips row
            val statusResId = try { WorkStatus.valueOf(piece.workStatus).labelResId } catch (_: Exception) { null }
            val statusName = statusResId?.let { stringResource(it) } ?: piece.workStatus
            val destResId = try { Destination.valueOf(piece.destination).labelResId } catch (_: Exception) { null }
            val destBaseName = destResId?.let { stringResource(it) } ?: piece.destination
            val destName = if (piece.quantityTotal > 1) "$destBaseName ${piece.quantitySold}/${piece.quantityTotal}" else destBaseName
            val typeResId = try { PieceType.valueOf(piece.type).labelResId } catch (_: Exception) { null }
            val typeName = typeResId?.let { stringResource(it) } ?: piece.type
            val statusBadgeStyle = if (piece.workStatus == "IN_PROGRESS") BadgeStyle.FILLED_SLATE else BadgeStyle.OUTLINE

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(text = statusName, style = statusBadgeStyle)
                StatusBadge(text = destName, style = BadgeStyle.OUTLINE)
                StatusBadge(text = typeName, style = BadgeStyle.MUTED)
            }

            // Row counter + timer — only while in progress
            if (piece.workStatus != "FINISHED") {
                RowCounterCard(
                    rowCount = piece.rowCount,
                    targetRowCount = piece.targetRowCount,
                    onIncrement = onIncrementRow,
                    onDecrement = onDecrementRow,
                    onIncrementBy = onIncrementRowBy,
                    onSetRowCount = onSetRowCount,
                    onShowSnackbar = onShowSnackbar
                )

                WorkTimerCard(
                    timerDisplaySeconds = timerDisplaySeconds,
                    timerState = timerState,
                    workHours = piece.workHours,
                    workSessions = workSessions,
                    pieceType = piece.type,
                    onStart = onStartTimer,
                    onPause = onPauseTimer,
                    onResume = onResumeTimer,
                    onStop = onStopTimer,
                    onEditTime = onEditWorkTime,
                )

                if (isPremium) {
                    ExtraCountersCard(
                        counters = counters,
                        onAdd = onAddCounter,
                        onDelete = onDeleteCounter,
                        onIncrement = onIncrementCounter,
                        onDecrement = onDecrementCounter,
                        onReset = onResetCounter,
                        onUpdate = onUpdateCounter,
                    )
                }
            }

            // Price suggestion — available whenever there are hours logged
            if (piece.workHours != null && piece.workHours > 0f) {
                TextButton(
                    onClick = { showPriceSuggestion = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Suggest price →",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandPurple,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Info card
            val hasInfo = piece.widthCm != null || piece.lengthCm != null ||
                piece.dateStarted != null || piece.dateFinished != null ||
                piece.hookSizeMm != null || piece.workHours != null
            if (hasInfo) {
                InfoCard(piece = piece)
            }

            // Work sessions section
            if (workSessions.isNotEmpty()) {
                WorkSessionsSection(workSessions = workSessions)
            }

            // Yarns used
            if (yarnsUsed.isNotEmpty()) {
                ChipListSection(
                    title = stringResource(R.string.piece_section_yarns_used),
                    items = yarnsUsed.map { id -> yarnNames[id] ?: id }
                )
            }

            // Stitches used
            if (stitchesUsed.isNotEmpty()) {
                ChipListSection(
                    title = stringResource(R.string.piece_section_stitches_used),
                    items = stitchesUsed.map { id -> stitchNames[id] ?: id }
                )
            }

            // Needles used
            if (needlesUsed.isNotEmpty()) {
                ChipListSection(
                    title = stringResource(R.string.piece_section_needles_used),
                    items = needlesUsed.map { id -> needleNames[id] ?: id }
                )
            }

            // Pricing section — only for pieces intended for sale or already sold
            val isSaleDestination = piece.destination == "FOR_SALE" || piece.destination == "SOLD"
            val hasPricing = piece.price != null || piece.materialCost != null ||
                piece.salePlatform != null || piece.saleLink != null ||
                piece.soldDate != null || piece.soldPrice != null
            if (isSaleDestination && hasPricing) {
                PricingCard(piece = piece)
            }

            // Gift section
            piece.giftRecipient?.let { recipient ->
                InfoRow(label = stringResource(R.string.piece_label_gift_for), value = recipient)
            }

            // Notes
            piece.notes?.let { notes ->
                NotesCard(notes = notes)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price suggestion dialog
            if (showPriceSuggestion) {
                PriceSuggestionDialog(
                    workHours = piece.workHours ?: 0f,
                    rowCount = piece.rowCount,
                    materialCost = piece.materialCost,
                    onApply = { price ->
                        onApplySuggestedPrice(price)
                        showPriceSuggestion = false
                    },
                    onDismiss = { showPriceSuggestion = false }
                )
            }

            // Done button
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.action_done), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }

            // Share button
            OutlinedButton(
                onClick = {
                    shareScope.launch {
                        isSharing = true
                        val intent = ShareCardGenerator.generateAndShare(
                            context = context,
                            pieceName = piece.name,
                            pieceType = piece.type,
                            destination = piece.destination,
                            workHours = piece.workHours,
                            rowCount = piece.rowCount,
                            photoPath = photos.firstOrNull()
                        )
                        isSharing = false
                        if (intent != null) {
                            context.startActivity(Intent.createChooser(intent, null))
                        }
                    }
                },
                enabled = !isSharing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandPurple),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurple)
            ) {
                if (isSharing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandPurple, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(stringResource(R.string.action_share_piece), style = MaterialTheme.typography.labelLarge)
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.action_edit),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onCloneClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.action_clone),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onArchiveClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text(
                        stringResource(R.string.action_archive),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun InfoCard(piece: PieceEntity) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (piece.widthCm != null || piece.lengthCm != null) {
                val widthText = piece.widthCm?.let { stringResource(R.string.piece_dimension_wide, it.toString()) }
                val lengthText = piece.lengthCm?.let { stringResource(R.string.piece_dimension_long, it.toString()) }
                val dims = listOfNotNull(widthText, lengthText).joinToString(" × ")
                InfoRow(label = stringResource(R.string.label_dimensions), value = dims)
            }
            piece.dateStarted?.let { InfoRow(label = stringResource(R.string.label_started), value = formatDate(it)) }
            piece.dateFinished?.let { InfoRow(label = stringResource(R.string.label_finished), value = formatDate(it)) }
            piece.hookSizeMm?.let { InfoRow(label = stringResource(R.string.label_hook_size), value = "${it}mm") }
            piece.workHours?.let { InfoRow(label = stringResource(R.string.label_hours), value = formatWorkTime(it)) }
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

@Composable
private fun WorkSessionsSection(workSessions: List<WorkSession>) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.piece_work_sessions_title, workSessions.size),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) stringResource(R.string.action_collapse) else stringResource(R.string.action_expand),
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    workSessions.forEach { session ->
                        WorkSessionItem(session = session)
                        if (session != workSessions.last()) {
                            Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkSessionItem(session: WorkSession) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDate(session.date),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = stringResource(R.string.piece_session_duration, session.durationMinutes),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        session.notes?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipListSection(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                StatusBadge(text = item, style = BadgeStyle.OUTLINE)
            }
        }
    }
}

@Composable
private fun PricingCard(piece: PieceEntity) {
    val sym = getCurrencySymbol(LocalContext.current)
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
                text = stringResource(R.string.piece_section_pricing),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val perUnitSuffix = if (piece.quantityTotal > 1) stringResource(R.string.piece_suffix_per_unit) else ""
            if (piece.quantityTotal > 1) {
                InfoRow(
                    label = stringResource(R.string.piece_field_quantity_total),
                    value = "${piece.quantitySold}/${piece.quantityTotal}"
                )
            }
            piece.materialCost?.let { InfoRow(label = stringResource(R.string.piece_label_material_cost) + perUnitSuffix, value = it.formatCurrency(sym)) }
            piece.price?.let { InfoRow(label = stringResource(R.string.piece_label_suggested_price) + perUnitSuffix, value = it.formatCurrency(sym)) }
            piece.salePlatform?.let { InfoRow(label = stringResource(R.string.piece_label_sale_platform), value = it) }
            piece.saleLink?.let { InfoRow(label = stringResource(R.string.piece_label_sale_link), value = it) }
            if (piece.destination == "SOLD") {
                piece.soldDate?.let { InfoRow(label = stringResource(R.string.piece_label_sold_on), value = formatDate(it)) }
                piece.soldPrice?.let { InfoRow(label = stringResource(R.string.piece_label_sold_price), value = it.formatCurrency(sym)) }
            }
            if (piece.quantityTotal > 1) {
                val remainingUnits = (piece.quantityTotal - piece.quantitySold).coerceAtLeast(0)
                val unitPrice = piece.price ?: 0f
                if (unitPrice > 0f) {
                    Text(
                        text = stringResource(
                            R.string.piece_potential_revenue_remaining,
                            (unitPrice * remainingUnits).formatCurrency(sym)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.label_notes),
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
private fun WorkTimerCard(
    timerDisplaySeconds: Long,
    timerState: TimerState,
    workHours: Float?,
    workSessions: List<WorkSession>,
    pieceType: String,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onEditTime: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    val showPaceEstimate = try {
        PieceType.valueOf(pieceType) in setOf(
            PieceType.SCARF, PieceType.BLANKET, PieceType.COWL, PieceType.HEADBAND
        )
    } catch (_: Exception) { false }
    val hours = timerDisplaySeconds / 3600
    val minutes = (timerDisplaySeconds % 3600) / 60
    val seconds = timerDisplaySeconds % 60
    val timeFormatted = "%02d:%02d:%02d".format(hours, minutes, seconds)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.piece_work_timer),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.piece_edit_work_time),
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (timerState == TimerState.RUNNING) BrandPurple else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (timerState) {
                    TimerState.IDLE -> {
                        Button(
                            onClick = onStart,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.action_start))
                        }
                    }
                    TimerState.RUNNING -> {
                        OutlinedButton(
                            onClick = onPause,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.action_pause))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onStop,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.action_stop))
                        }
                    }
                    TimerState.PAUSED -> {
                        Button(
                            onClick = onResume,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.action_resume))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = onStop,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.action_stop))
                        }
                    }
                }
            }
            if (workHours != null && timerState == TimerState.IDLE) {
                Spacer(modifier = Modifier.height(8.dp))
                val displayHours = formatWorkTime(workHours)
                Text(
                    text = stringResource(R.string.piece_timer_total, displayHours),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                if (showPaceEstimate) {
                    val sessionsWithRows = workSessions.filter { (it.rowsCompleted ?: 0) > 0 && it.durationMinutes > 0 }
                    if (sessionsWithRows.isNotEmpty()) {
                        val totalMinutes = sessionsWithRows.sumOf { it.durationMinutes }
                        val totalRows = sessionsWithRows.sumOf { it.rowsCompleted ?: 0 }
                        val minutesPer10 = (totalMinutes.toFloat() / totalRows * 10).toInt().coerceAtLeast(1)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "~$minutesPer10 min / 10 rows",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditWorkTimeDialog(
            currentTotalSeconds = timerDisplaySeconds,
            onDismiss = { showEditDialog = false },
            onSave = { totalSeconds ->
                onEditTime(totalSeconds)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun RowCounterCard(
    rowCount: Int,
    targetRowCount: Int?,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onIncrementBy: (Int) -> Unit,
    onSetRowCount: (Int, Int?) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val voiceErrorMsg = stringResource(R.string.piece_voice_error)
    val voicePermissionMsg = stringResource(R.string.piece_voice_permission)
    val voiceTimeoutMsg = stringResource(R.string.piece_voice_timeout)
    val voiceLangPackMsg = stringResource(R.string.piece_voice_lang_pack)
    val timeoutJob = remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val speechAvailable = remember(context) { SpeechRecognizer.isRecognitionAvailable(context) }
    val speechRecognizer = remember(context) {
        if (speechAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    val speechLocaleTag = remember {
        val locales = AppCompatDelegate.getApplicationLocales()
        val tag = if (locales.isEmpty) java.util.Locale.getDefault().toLanguageTag()
        else locales[0]?.toLanguageTag() ?: java.util.Locale.getDefault().toLanguageTag()
        when {
            tag.startsWith("pt") -> "pt-BR"
            tag.startsWith("es") -> "es-ES"
            tag.startsWith("en") -> "en-US"
            else -> tag
        }
    }
    DisposableEffect(speechRecognizer) {
        onDispose { speechRecognizer?.destroy() }
    }

    // Stop mic when app goes to background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && isListening) {
                isListening = false
                timeoutJob.value?.cancel()
                speechRecognizer?.cancel()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (!granted) scope.launch { onShowSnackbar(voicePermissionMsg) }
    }

    fun parseSpokenNumber(text: String): Int? {
        Regex("\\d+").find(text)?.value?.toIntOrNull()?.let { return it }
        val words = mapOf(
            // English
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
            // Spanish
            "uno" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5,
            "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10,
            // Portuguese
            "um" to 1, "uma" to 1, "dois" to 2, "duas" to 2, "três" to 3,
            "quatro" to 4, "cinco" to 5, "seis" to 6, "sete" to 7, "oito" to 8,
            "nove" to 9, "dez" to 10
        )
        val lower = text.lowercase()
        return words.entries.firstOrNull { lower.contains(it.key) }?.value
    }

    fun resetTimeout(sr: SpeechRecognizer) {
        timeoutJob.value?.cancel()
        timeoutJob.value = scope.launch {
            delay(45_000)
            isListening = false
            sr.cancel()
            onShowSnackbar(voiceTimeoutMsg)
        }
    }

    fun startListening() {
        val sr = speechRecognizer ?: return
        isListening = true
        resetTimeout(sr)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocaleTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, speechLocaleTag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val candidates = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?: emptyList()
                val number = candidates.firstNotNullOfOrNull { parseSpokenNumber(it) }
                if (number != null && number > 0) {
                    onIncrementBy(number)
                    resetTimeout(sr) // reset inactivity clock on each successful count
                }
                if (isListening) scope.launch { delay(150); sr.startListening(intent) }
            }
            override fun onError(error: Int) {
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        if (isListening) scope.launch { delay(150); sr.startListening(intent) }
                    SpeechRecognizer.ERROR_CLIENT -> { /* user cancelled, do nothing */ }
                    12, 13 -> { // ERROR_LANGUAGE_NOT_SUPPORTED / ERROR_LANGUAGE_UNAVAILABLE (API 31+)
                        isListening = false
                        timeoutJob.value?.cancel()
                        scope.launch { onShowSnackbar(voiceLangPackMsg) }
                    }
                    else -> {
                        isListening = false
                        timeoutJob.value?.cancel()
                        scope.launch { onShowSnackbar(voiceErrorMsg) }
                    }
                }
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        sr.startListening(intent)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.piece_row_counter),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.piece_edit_row_count),
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Counter row: [−]  number  [+]  [mic]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // − button
                OutlinedButton(
                    onClick = onDecrement,
                    enabled = rowCount > 0,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = stringResource(R.string.action_decrement),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Row number
                Text(
                    text = rowCount.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1.5f)
                )

                // + button
                Button(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.action_increment),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Mic button (only if speech available)
                if (speechAvailable) {
                    IconButton(
                        onClick = {
                            when {
                                isListening -> { isListening = false; timeoutJob.value?.cancel(); speechRecognizer?.cancel() }
                                !hasAudioPermission -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                else -> startListening()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isListening) BrandPurple.copy(alpha = 0.12f) else Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = if (isListening) stringResource(R.string.action_stop_listening) else stringResource(R.string.action_voice_count),
                            tint = if (isListening) BrandPurple else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Progress bar (only when targetRowCount is set)
            if (targetRowCount != null && targetRowCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress by animateFloatAsState(
                    targetValue = (rowCount.toFloat() / targetRowCount).coerceIn(0f, 1f),
                    label = "rowProgress"
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.piece_rows_progress, rowCount, targetRowCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }

    if (showEditDialog) {
        SetRowCountDialog(
            currentCount = rowCount,
            currentTarget = targetRowCount,
            onDismiss = { showEditDialog = false },
            onSave = { count, target ->
                onSetRowCount(count, target)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SetRowCountDialog(
    currentCount: Int,
    currentTarget: Int?,
    onDismiss: () -> Unit,
    onSave: (Int, Int?) -> Unit
) {
    var countText by remember { mutableStateOf(currentCount.toString()) }
    var targetText by remember { mutableStateOf(currentTarget?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.piece_row_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = countText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) countText = it },
                    label = { Text(stringResource(R.string.piece_row_current)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) targetText = it },
                    label = { Text(stringResource(R.string.piece_row_target)) },
                    placeholder = { Text(stringResource(R.string.piece_row_target_hint), color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = countText.toIntOrNull() ?: currentCount
                    val target = targetText.toIntOrNull()
                    onSave(count, target)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = TextSecondary) }
        }
    )
}

@Composable
private fun EditWorkTimeDialog(
    currentTotalSeconds: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val currentHours = currentTotalSeconds / 3600
    val currentMinutes = (currentTotalSeconds % 3600) / 60
    var hoursText by remember { mutableStateOf(currentHours.toString()) }
    var minutesText by remember { mutableStateOf(currentMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.piece_work_time_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) hoursText = it },
                    label = { WordSafeText(stringResource(R.string.piece_work_time_hours)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) minutesText = it },
                    label = { WordSafeText(stringResource(R.string.piece_work_time_minutes)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hoursText.toLongOrNull() ?: currentHours
                    val m = (minutesText.toLongOrNull() ?: currentMinutes).coerceIn(0, 59)
                    onSave(h * 3600 + m * 60)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = TextSecondary) }
        }
    )
}

@Composable
private fun ArchiveDialog(
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.piece_archive_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.piece_archive_message),
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
                        focusedBorderColor = BrandPurple,
                        cursorColor = BrandPurple
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

@Composable
private fun PriceSuggestionDialog(
    workHours: Float,
    rowCount: Int,
    materialCost: Float?,
    onApply: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sym = getCurrencySymbol(context)
    val prefs = remember { context.getSharedPreferences("hooky_settings", android.content.Context.MODE_PRIVATE) }
    var hourlyRateText by remember {
        mutableStateOf(prefs.getFloat("hourly_rate", 12f).let {
            if (it % 1f == 0f) it.toInt().toString() else "%.2f".format(it)
        })
    }

    val hourlyRate = hourlyRateText.toFloatOrNull() ?: 0f
    val suggested = hourlyRate * workHours + (materialCost ?: 0f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                stringResource(R.string.piece_price_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.calculator_work_hours), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        formatWorkTime(workHours),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.piece_price_dialog_rows_worked), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(
                        rowCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (materialCost != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.piece_label_material_cost), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            materialCost.formatCurrency(sym),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                OutlinedTextField(
                    value = hourlyRateText,
                    onValueChange = { hourlyRateText = it },
                    label = { Text(stringResource(R.string.piece_price_dialog_hourly_rate, sym)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPurple,
                        cursorColor = BrandPurple,
                        focusedLabelColor = BrandPurple,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.piece_label_suggested_price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        suggested.formatCurrency(sym),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    prefs.edit().putFloat("hourly_rate", hourlyRate).apply()
                    onApply(suggested)
                },
                enabled = hourlyRate > 0f && suggested > 0f,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text(stringResource(R.string.piece_price_dialog_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ExtraCountersCard(
    counters: List<CounterEntity>,
    onAdd: (String, Int?) -> Unit,
    onDelete: (Int) -> Unit,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onReset: (Int) -> Unit,
    onUpdate: (CounterEntity) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCounter by remember { mutableStateOf<CounterEntity?>(null) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.piece_extra_counters),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.piece_counter_add),
                        tint = Slate,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (counters.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.piece_counter_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                counters.forEach { counter ->
                    CounterItemRow(
                        counter = counter,
                        onIncrement = { onIncrement(counter.id) },
                        onDecrement = { onDecrement(counter.id) },
                        onDelete = { onDelete(counter.id) },
                        onEdit = { editingCounter = counter },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        CounterFormDialog(
            title = stringResource(R.string.piece_counter_add),
            initialName = "",
            initialTarget = null,
            onConfirm = { name, target -> onAdd(name, target); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    editingCounter?.let { counter ->
        CounterFormDialog(
            title = stringResource(R.string.piece_counter_edit),
            initialName = counter.name,
            initialTarget = counter.target,
            onConfirm = { name, target ->
                onUpdate(counter.copy(name = name, target = target))
                editingCounter = null
            },
            onDismiss = { editingCounter = null }
        )
    }
}

@Composable
private fun CounterItemRow(
    counter: CounterEntity,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onDecrement,
            enabled = counter.count > 0,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.action_decrement),
                modifier = Modifier.size(18.dp)
            )
        }

        val countLabel = if (counter.target != null)
            "${counter.count}/${counter.target}" else counter.count.toString()
        Text(
            text = countLabel,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.action_increment),
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = counter.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .clickable { onEdit() }
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CounterFormDialog(
    title: String,
    initialName: String,
    initialTarget: Int?,
    onConfirm: (String, Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var targetText by remember { mutableStateOf(initialTarget?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.piece_counter_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate,
                        focusedLabelColor = Slate
                    )
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.piece_counter_target_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Slate,
                        focusedLabelColor = Slate
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim(), targetText.toIntOrNull())
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Slate, contentColor = White)
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = TextSecondary)
            }
        }
    )
}
