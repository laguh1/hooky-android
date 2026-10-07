package com.hooky.app.ui.search

import com.hooky.app.ui.yarns.yarnColorLabel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hooky.app.R
import coil.compose.AsyncImage
import com.hooky.app.data.db.entity.PieceEntity
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.util.labelResId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPieceDetail: (Int) -> Unit,
    onNavigateToYarnDetail: (Int) -> Unit,
    onNavigateToStitchDetail: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.search_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search input
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onAction(SearchAction.QueryChanged(it)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_hint),
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = uiState.query.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { viewModel.onAction(SearchAction.ClearQuery) }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear search",
                                tint = TextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = BrandPurple,
                    cursorColor = BrandPurple
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Slate)
                        }
                    }

                    uiState.query.length < 2 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = stringResource(R.string.search_prompt),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Enter at least 2 characters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    uiState.hasSearched &&
                        uiState.pieceResults.isEmpty() &&
                        uiState.yarnResults.isEmpty() &&
                        uiState.stitchResults.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = stringResource(R.string.search_no_results),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Nothing matched \"${uiState.query}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    else -> {
                        SearchResultList(
                            pieceResults = uiState.pieceResults,
                            yarnResults = uiState.yarnResults,
                            stitchResults = uiState.stitchResults,
                            onPieceClick = onNavigateToPieceDetail,
                            onYarnClick = onNavigateToYarnDetail,
                            onStitchClick = onNavigateToStitchDetail
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Results list
// ---------------------------------------------------------------------------

@Composable
private fun SearchResultList(
    pieceResults: List<PieceEntity>,
    yarnResults: List<YarnEntity>,
    stitchResults: List<StitchEntity>,
    onPieceClick: (Int) -> Unit,
    onYarnClick: (Int) -> Unit,
    onStitchClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Pieces section
        if (pieceResults.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    label = "Pieces",
                    count = pieceResults.size
                )
            }
            items(items = pieceResults, key = { "piece-${it.id}" }) { piece ->
                PieceResultRow(
                    piece = piece,
                    onClick = { onPieceClick(piece.id) }
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        // Yarns section
        if (yarnResults.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    label = "Yarns",
                    count = yarnResults.size
                )
            }
            items(items = yarnResults, key = { "yarn-${it.id}" }) { yarn ->
                YarnResultRow(
                    yarn = yarn,
                    onClick = { onYarnClick(yarn.id) }
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        // Stitches section
        if (stitchResults.isNotEmpty()) {
            item {
                SearchSectionHeader(
                    label = "Stitches",
                    count = stitchResults.size
                )
            }
            items(items = stitchResults, key = { "stitch-${it.id}" }) { stitch ->
                StitchResultRow(
                    stitch = stitch,
                    onClick = { onStitchClick(stitch.id) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section header
// ---------------------------------------------------------------------------

@Composable
private fun SearchSectionHeader(
    label: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )
    }
}

// ---------------------------------------------------------------------------
// Piece result row
// ---------------------------------------------------------------------------

@Composable
private fun PieceResultRow(
    piece: PieceEntity,
    onClick: () -> Unit
) {
    val photoList = try {
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString<List<String>>(piece.photos)
    } catch (_: Exception) {
        emptyList()
    }

    val typeResId = try { com.hooky.app.domain.model.enums.PieceType.valueOf(piece.type).labelResId } catch (_: Exception) { null }
    val typeDisplay = typeResId?.let { stringResource(it) } ?: piece.type

    val statusResId = try { com.hooky.app.domain.model.enums.WorkStatus.valueOf(piece.workStatus).labelResId } catch (_: Exception) { null }
    val statusDisplay = statusResId?.let { stringResource(it) } ?: piece.workStatus

    SearchResultRow(
        photoPath = photoList.firstOrNull(),
        placeholderIcon = painterResource(R.drawable.ic_pieces),
        name = piece.name,
        subtitle = "$typeDisplay · $statusDisplay",
        onClick = onClick
    )
}

// ---------------------------------------------------------------------------
// Yarn result row
// ---------------------------------------------------------------------------

@Composable
private fun YarnResultRow(
    yarn: YarnEntity,
    onClick: () -> Unit
) {
    val photoList = try {
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString<List<String>>(yarn.photos)
    } catch (_: Exception) {
        emptyList()
    }

    val brand = yarn.brand ?: stringResource(R.string.yarn_brand_unknown)
    val subtitle = "$brand · ${yarnColorLabel(yarn.color)}"

    SearchResultRow(
        photoPath = photoList.firstOrNull(),
        placeholderIcon = painterResource(R.drawable.ic_yarns),
        name = yarn.name,
        subtitle = subtitle,
        onClick = onClick
    )
}

// ---------------------------------------------------------------------------
// Stitch result row
// ---------------------------------------------------------------------------

@Composable
private fun StitchResultRow(
    stitch: StitchEntity,
    onClick: () -> Unit
) {
    val photoList = try {
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString<List<String>>(stitch.photos)
    } catch (_: Exception) {
        emptyList()
    }

    val categoryResId = remember(stitch.category) {
        stitch.category?.let { try { com.hooky.app.domain.model.enums.StitchCategory.valueOf(it).labelResId } catch (_: Exception) { null } }
    }
    val categoryText = categoryResId?.let { stringResource(it) } ?: stitch.category ?: ""
    val difficultyResId = remember(stitch.difficulty) {
        stitch.difficulty?.let { try { com.hooky.app.domain.model.enums.Difficulty.valueOf(it).labelResId } catch (_: Exception) { null } }
    }
    val difficultyText = difficultyResId?.let { stringResource(it) } ?: stitch.difficulty ?: ""
    val subtitle = listOf(categoryText, difficultyText).filter { it.isNotBlank() }.joinToString(" · ")

    SearchResultRow(
        photoPath = photoList.firstOrNull(),
        placeholderIcon = painterResource(R.drawable.ic_stitches),
        name = stitch.name,
        subtitle = subtitle,
        onClick = onClick
    )
}

// ---------------------------------------------------------------------------
// Generic result row card
// ---------------------------------------------------------------------------

@Composable
private fun SearchResultRow(
    photoPath: String?,
    placeholderIcon: Painter,
    name: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BorderLight),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo / placeholder — 48dp
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photoPath != null) {
                    AsyncImage(
                        model = photoPath,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = placeholderIcon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Text block
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
