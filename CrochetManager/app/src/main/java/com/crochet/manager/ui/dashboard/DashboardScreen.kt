package com.crochet.manager.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.crochet.manager.data.db.entity.PieceEntity
import com.crochet.manager.domain.model.enums.Destination
import com.crochet.manager.domain.model.enums.WorkStatus
import com.crochet.manager.ui.components.BadgeStyle
import com.crochet.manager.ui.components.StatusBadge
import com.crochet.manager.ui.theme.BackgroundLight
import com.crochet.manager.ui.theme.BorderLight
import com.crochet.manager.ui.theme.BorderStrong
import com.crochet.manager.ui.theme.Slate
import com.crochet.manager.ui.theme.TextMuted
import com.crochet.manager.ui.theme.TextSecondary
import com.crochet.manager.ui.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------------------------------------------------------------------------
// Format helpers
// ---------------------------------------------------------------------------

private fun formatHours(hours: Float): String {
    return if (hours % 1f == 0f) {
        "${hours.toInt()}h"
    } else {
        "${hours}h"
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
    } catch (_: Exception) {
        dateStr
    }
}

private fun formatRevenue(amount: Float): String {
    return "€%.2f".format(amount)
}

private fun parsePhotos(json: String): List<String> {
    return try {
        kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        }.decodeFromString<List<String>>(json)
    } catch (_: Exception) {
        emptyList()
    }
}

// ---------------------------------------------------------------------------
// Screen entry point
// ---------------------------------------------------------------------------

@Composable
fun DashboardScreen(
    onNavigateToPieces: () -> Unit,
    onNavigateToYarns: () -> Unit,
    onNavigateToStitches: () -> Unit,
    onNavigateToPieceDetail: (Int) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToPriceCalculator: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
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
            DashboardContent(
                uiState = uiState,
                onNavigateToPieces = onNavigateToPieces,
                onNavigateToPieceDetail = onNavigateToPieceDetail,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToPriceCalculator = onNavigateToPriceCalculator,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Main content (LazyColumn)
// ---------------------------------------------------------------------------

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToPieces: () -> Unit,
    onNavigateToPieceDetail: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPriceCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ---- Header ----
        item {
            DashboardHeader(onNavigateToSearch = onNavigateToSearch)
        }

        // ---- Stats row ----
        item {
            StatsRow(
                pieceCount = uiState.pieceCount,
                yarnCount = uiState.yarnCount,
                stitchCount = uiState.stitchCount,
                totalWorkHours = uiState.totalWorkHours,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        // ---- In Progress section ----
        item {
            SectionHeader(
                title = "In Progress",
                onSeeAll = onNavigateToPieces,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        item {
            if (uiState.inProgressPieces.isEmpty()) {
                Text(
                    text = "Nothing in progress — start a new piece!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .padding(bottom = 8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = uiState.inProgressPieces, key = { it.id }) { piece ->
                        InProgressCard(
                            piece = piece,
                            onClick = { onNavigateToPieceDetail(piece.id) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // ---- Recently Finished section ----
        item {
            SectionHeader(
                title = "Recently Finished",
                onSeeAll = onNavigateToPieces,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        if (uiState.recentlyFinished.isEmpty()) {
            item {
                Text(
                    text = "No finished pieces yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .padding(bottom = 8.dp)
                )
            }
        } else {
            items(items = uiState.recentlyFinished, key = { it.id }) { piece ->
                RecentlyFinishedRow(
                    piece = piece,
                    onClick = { onNavigateToPieceDetail(piece.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // ---- Revenue row (only if > 0) ----
        if (uiState.soldRevenue > 0f) {
            item {
                Spacer(Modifier.height(20.dp))
                RevenueCard(
                    revenue = uiState.soldRevenue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }

        // ---- Price Calculator shortcut ----
        item {
            Spacer(Modifier.height(20.dp))
            PriceCalculatorShortcut(
                onClick = onNavigateToPriceCalculator,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

@Composable
private fun DashboardHeader(
    onNavigateToSearch: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Eyebrow row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MY CROCHET STUDIO",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontSize = 10.sp
                ),
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search icon button
                IconButton(onClick = onNavigateToSearch) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                // Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Slate),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "J",
                        style = MaterialTheme.typography.labelMedium,
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Large title
        Text(
            text = "My Crochet Studio",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )

        Divider(
            color = BorderLight,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---------------------------------------------------------------------------
// Stats row — 4 equal-weight cards
// ---------------------------------------------------------------------------

@Composable
private fun StatsRow(
    pieceCount: Int,
    yarnCount: Int,
    stitchCount: Int,
    totalWorkHours: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            number = pieceCount.toString(),
            label = "PIECES",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            number = yarnCount.toString(),
            label = "YARNS",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            number = stitchCount.toString(),
            label = "STITCHES",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            number = if (totalWorkHours == 0f) "--" else formatHours(totalWorkHours),
            label = "WORKED",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, BorderLight),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Slate,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.8.sp,
                    fontSize = 9.sp
                ),
                color = TextMuted,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Section header
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onSeeAll) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// In-Progress card (horizontal scroll, 160dp wide)
// ---------------------------------------------------------------------------

@Composable
private fun InProgressCard(
    piece: PieceEntity,
    onClick: () -> Unit
) {
    val photoList = remember(piece.photos) { parsePhotos(piece.photos) }

    val workStatusDisplay = try {
        WorkStatus.valueOf(piece.workStatus).displayName
    } catch (_: Exception) {
        piece.workStatus
    }
    val destinationDisplay = try {
        Destination.valueOf(piece.destination).displayName
    } catch (_: Exception) {
        piece.destination
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column {
            // Photo area — 100dp tall square
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color(0xFF2D2D2D)),
                contentAlignment = Alignment.Center
            ) {
                if (photoList.isNotEmpty()) {
                    AsyncImage(
                        model = photoList.first(),
                        contentDescription = piece.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Texture,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Text content
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = piece.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Status + Destination chips in a row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusBadge(
                        text = workStatusDisplay,
                        style = BadgeStyle.FILLED_SLATE
                    )
                    StatusBadge(
                        text = destinationDisplay,
                        style = BadgeStyle.OUTLINE
                    )
                }

                // Work hours (if present)
                piece.workHours?.let { hours ->
                    if (hours > 0f) {
                        Text(
                            text = formatHours(hours),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Recently Finished row
// ---------------------------------------------------------------------------

@Composable
private fun RecentlyFinishedRow(
    piece: PieceEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoList = remember(piece.photos) { parsePhotos(piece.photos) }

    val workStatusDisplay = try {
        WorkStatus.valueOf(piece.workStatus).displayName
    } catch (_: Exception) {
        piece.workStatus
    }
    val destinationDisplay = try {
        Destination.valueOf(piece.destination).displayName
    } catch (_: Exception) {
        piece.destination
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderLight),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Photo — 64dp × 64dp
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2D2D2D)),
                contentAlignment = Alignment.Center
            ) {
                if (photoList.isNotEmpty()) {
                    AsyncImage(
                        model = photoList.first(),
                        contentDescription = piece.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Texture,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Right column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = piece.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Type + Destination badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusBadge(text = workStatusDisplay, style = BadgeStyle.OUTLINE)
                    StatusBadge(text = destinationDisplay, style = BadgeStyle.MUTED)
                }

                // Date finished
                piece.dateFinished?.let { date ->
                    Text(
                        text = "Finished ${formatDate(date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Price Calculator shortcut
// ---------------------------------------------------------------------------

@Composable
private fun PriceCalculatorShortcut(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Slate
        ),
        border = BorderStroke(1.dp, BorderStrong)
    ) {
        Icon(
            imageVector = Icons.Filled.Calculate,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Price Calculator",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---------------------------------------------------------------------------
// Revenue card
// ---------------------------------------------------------------------------

@Composable
private fun RevenueCard(
    revenue: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderStrong),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Revenue",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatRevenue(revenue),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate
            )
        }
    }
}
