package com.crochet.manager.ui.pieces.detail

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.crochet.manager.data.db.entity.PieceEntity
import com.crochet.manager.domain.model.WorkSession
import com.crochet.manager.domain.model.enums.Destination
import com.crochet.manager.domain.model.enums.PieceType
import com.crochet.manager.domain.model.enums.WorkStatus
import com.crochet.manager.ui.components.BadgeStyle
import com.crochet.manager.ui.components.StatusBadge
import com.crochet.manager.ui.theme.BackgroundLight
import com.crochet.manager.ui.theme.BorderLight
import com.crochet.manager.ui.theme.ErrorRed
import com.crochet.manager.ui.theme.Slate
import com.crochet.manager.ui.theme.TextMuted
import com.crochet.manager.ui.theme.TextSecondary
import com.crochet.manager.ui.theme.White

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

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(PieceDetailAction.ClearError)
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
            uiState.piece == null -> {
                Text(
                    "Piece not found",
                    modifier = Modifier.align(Alignment.Center),
                    color = TextSecondary
                )
            }
            else -> {
                PieceDetailContent(
                    piece = uiState.piece!!,
                    onNavigateBack = onNavigateBack,
                    onNavigateToEdit = { onNavigateToEdit(pieceId) },
                    onArchiveClick = { viewModel.onAction(PieceDetailAction.ShowArchiveDialog) }
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
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onArchiveClick: () -> Unit
) {
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
                    contentDescription = "Back",
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
            val statusName = try { WorkStatus.valueOf(piece.workStatus).displayName } catch (_: Exception) { piece.workStatus }
            val destName = try { Destination.valueOf(piece.destination).displayName } catch (_: Exception) { piece.destination }
            val typeName = try { PieceType.valueOf(piece.type).displayName } catch (_: Exception) { piece.type }
            val statusBadgeStyle = if (piece.workStatus == "IN_PROGRESS") BadgeStyle.FILLED_SLATE else BadgeStyle.OUTLINE

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(text = statusName, style = statusBadgeStyle)
                StatusBadge(text = destName, style = BadgeStyle.OUTLINE)
                StatusBadge(text = typeName, style = BadgeStyle.MUTED)
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
                ChipListSection(title = "Yarns Used", items = yarnsUsed)
            }

            // Stitches used
            if (stitchesUsed.isNotEmpty()) {
                ChipListSection(title = "Stitches Used", items = stitchesUsed)
            }

            // Pricing section
            val hasPricing = piece.price != null || piece.materialCost != null ||
                piece.salePlatform != null || piece.saleLink != null ||
                piece.soldDate != null || piece.soldPrice != null
            if (hasPricing) {
                PricingCard(piece = piece)
            }

            // Gift section
            piece.giftRecipient?.let { recipient ->
                InfoRow(label = "Gift for", value = recipient)
            }

            // Notes
            piece.notes?.let { notes ->
                NotesCard(notes = notes)
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
                    Text("Edit")
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
                    Text("Archive")
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
        color = BackgroundLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (piece.widthCm != null || piece.lengthCm != null) {
                val dims = buildString {
                    piece.widthCm?.let { append("${it}cm wide") }
                    if (piece.widthCm != null && piece.lengthCm != null) append(" × ")
                    piece.lengthCm?.let { append("${it}cm long") }
                }
                InfoRow(label = "Dimensions", value = dims)
            }
            piece.dateStarted?.let { InfoRow(label = "Started", value = it) }
            piece.dateFinished?.let { InfoRow(label = "Finished", value = it) }
            piece.hookSizeMm?.let { InfoRow(label = "Hook size", value = "${it}mm") }
            piece.workHours?.let { InfoRow(label = "Work hours", value = "${it}h") }
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
        color = BackgroundLight,
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
                    text = "Work Sessions (${workSessions.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
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
                            Divider(color = BorderLight, thickness = 0.5.dp)
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
                text = session.date,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${session.durationMinutes} min",
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
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BackgroundLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Pricing",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            piece.materialCost?.let { InfoRow(label = "Material cost", value = "€$it") }
            piece.price?.let { InfoRow(label = "Suggested price", value = "€$it") }
            piece.salePlatform?.let { InfoRow(label = "Sale platform", value = it) }
            piece.saleLink?.let { InfoRow(label = "Sale link", value = it) }
            piece.soldDate?.let { InfoRow(label = "Sold on", value = it) }
            piece.soldPrice?.let { InfoRow(label = "Sold price", value = "€$it") }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = BackgroundLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Notes",
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
private fun ArchiveDialog(
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Archive this piece?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "The piece will be archived and hidden from your list. You can still find it in your archive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (optional)") },
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
                Text("Archive")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
