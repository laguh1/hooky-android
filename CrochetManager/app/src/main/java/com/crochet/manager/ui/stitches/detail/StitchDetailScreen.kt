package com.crochet.manager.ui.stitches.detail

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.crochet.manager.data.db.entity.StitchEntity
import com.crochet.manager.domain.model.enums.Difficulty
import com.crochet.manager.domain.model.enums.StitchCategory
import com.crochet.manager.ui.components.BadgeStyle
import com.crochet.manager.ui.components.StatusBadge
import com.crochet.manager.ui.theme.BackgroundLight
import com.crochet.manager.ui.theme.ErrorRed
import com.crochet.manager.ui.theme.Slate
import com.crochet.manager.ui.theme.TextMuted
import com.crochet.manager.ui.theme.TextSecondary
import com.crochet.manager.ui.theme.White
import kotlinx.serialization.json.Json

@Composable
fun StitchDetailScreen(
    stitchId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: StitchDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(StitchDetailAction.ClearError)
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
            uiState.stitch == null -> {
                Text(
                    "Stitch not found",
                    modifier = Modifier.align(Alignment.Center),
                    color = TextSecondary
                )
            }
            else -> {
                StitchDetailContent(
                    stitch = uiState.stitch!!,
                    onNavigateBack = onNavigateBack,
                    onNavigateToEdit = { onNavigateToEdit(stitchId) },
                    onArchiveClick = { viewModel.onAction(StitchDetailAction.ShowArchiveDialog) }
                )
            }
        }

        // Archive dialog
        if (uiState.showArchiveDialog) {
            StitchArchiveDialog(
                onConfirm = { reason ->
                    viewModel.onAction(StitchDetailAction.ConfirmArchive(reason))
                },
                onDismiss = { viewModel.onAction(StitchDetailAction.HideArchiveDialog) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StitchDetailContent(
    stitch: StitchEntity,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onArchiveClick: () -> Unit
) {
    val json = remember { Json { ignoreUnknownKeys = true } }

    val photos = remember(stitch.photos) {
        try { json.decodeFromString<List<String>>(stitch.photos) } catch (_: Exception) { emptyList() }
    }

    val aliases = remember(stitch.nameAliases) {
        try { json.decodeFromString<List<String>>(stitch.nameAliases) } catch (_: Exception) { emptyList() }
    }

    val categoryDisplayName = remember(stitch.category) {
        stitch.category?.let {
            try { StitchCategory.valueOf(it).displayName } catch (_: Exception) { it }
        }
    }

    val difficultyDisplayName = remember(stitch.difficulty) {
        stitch.difficulty?.let {
            try { Difficulty.valueOf(it).displayName } catch (_: Exception) { it }
        }
    }

    val uriHandler = LocalUriHandler.current

    val hasTutorialLinks = stitch.hookfullyLink != null ||
        stitch.instructionLink != null ||
        stitch.videoLink != null

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
                        imageVector = Icons.Filled.Straighten,
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
            // Stitch name
            Text(
                text = stitch.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Spanish name in italic secondary text below name
            stitch.nameEs?.let { nameEs ->
                Text(
                    text = nameEs,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = TextSecondary
                )
            }

            // Chips row: category + difficulty
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categoryDisplayName?.let { cat ->
                    StatusBadge(text = cat, style = BadgeStyle.MUTED)
                }
                difficultyDisplayName?.let { diff ->
                    StatusBadge(text = diff, style = BadgeStyle.OUTLINE)
                }
            }

            // Info card
            val hasInfoContent = stitch.abbreviation != null ||
                aliases.isNotEmpty() ||
                stitch.description.isNotBlank()
            if (hasInfoContent) {
                StitchInfoCard(title = "About This Stitch") {
                    stitch.abbreviation?.let { abbr ->
                        StitchInfoRow(label = "Abbreviation", value = abbr)
                    }
                    if (aliases.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Also known as",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                aliases.forEach { alias ->
                                    StatusBadge(text = alias, style = BadgeStyle.OUTLINE)
                                }
                            }
                        }
                    }
                    if (stitch.description.isNotBlank()) {
                        Text(
                            text = stitch.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Tutorial Links card
            if (hasTutorialLinks) {
                StitchInfoCard(title = "Learn This Stitch") {
                    stitch.hookfullyLink?.let { link ->
                        TutorialLinkRow(
                            icon = Icons.Filled.Link,
                            label = "View on Hookfully",
                            onClick = {
                                try { uriHandler.openUri(link) } catch (_: Exception) {}
                            }
                        )
                    }
                    stitch.instructionLink?.let { link ->
                        TutorialLinkRow(
                            icon = Icons.Filled.Link,
                            label = "View Instructions",
                            onClick = {
                                try { uriHandler.openUri(link) } catch (_: Exception) {}
                            }
                        )
                    }
                    stitch.videoLink?.let { link ->
                        TutorialLinkRow(
                            icon = Icons.Filled.PlayCircle,
                            label = "Watch Video",
                            onClick = {
                                try { uriHandler.openUri(link) } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }

            // Notes
            stitch.notes?.let { notes ->
                StitchInfoCard(title = "Notes") {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
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
private fun StitchInfoCard(
    title: String,
    content: @Composable () -> Unit
) {
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
private fun StitchInfoRow(label: String, value: String) {
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
private fun TutorialLinkRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Slate,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Slate
            )
        }
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun StitchArchiveDialog(
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Archive this stitch?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "The stitch will be archived and hidden from your list. You can still find it in your archive.",
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
