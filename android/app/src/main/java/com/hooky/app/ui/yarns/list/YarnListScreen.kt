package com.hooky.app.ui.yarns.list

import com.hooky.app.ui.yarns.ColorFamilyRow
import com.hooky.app.ui.yarns.YarnColorDot
import com.hooky.app.domain.model.YarnColor
import com.hooky.app.ui.yarns.yarnColorLabel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hooky.app.R
import coil.compose.AsyncImage
import com.hooky.app.data.db.entity.YarnEntity
import com.hooky.app.ui.components.BadgeStyle
import com.hooky.app.ui.components.StatusBadge
import com.hooky.app.ui.theme.BackgroundLight
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.util.labelResId
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnListScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: YarnListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // After changing the sort or colour filter, start from the top of the list (it used to stay
    // on the yarn that had been first, hiding everything above it). Only on a real change, so
    // coming back from a yarn keeps the scroll position.
    val gridState = rememberLazyGridState()
    val listKey = "${uiState.sortByColor}|${uiState.selectedColor}"
    var lastListKey by rememberSaveable { mutableStateOf(listKey) }
    LaunchedEffect(listKey) {
        if (listKey != lastListKey) {
            lastListKey = listKey
            gridState.scrollToItem(0)
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(YarnListAction.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.yarns_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Slate,
                contentColor = White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.yarn_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onAction(YarnListAction.SearchQueryChanged(it)) },
                placeholder = {
                    Text(
                        stringResource(R.string.yarn_search_hint),
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                YarnFilter.values().forEach { filter ->
                    val selected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onAction(YarnListAction.FilterSelected(filter)) },
                        label = {
                            Text(
                                text = stringResource(filter.labelRes),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = BorderLight,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }

            if (uiState.colorFamilies.isNotEmpty()) {
                ColorFamilyRow(
                    families = uiState.colorFamilies,
                    selected = uiState.selectedColor,
                    onSelected = { viewModel.onAction(YarnListAction.ColorSelected(it)) },
                    sortByColor = uiState.sortByColor,
                    onToggleSort = { viewModel.onAction(YarnListAction.ToggleSortByColor) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Slate)
                    }
                }

                uiState.yarns.isEmpty() -> {
                    YarnEmptyState()
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.yarns,
                            key = { it.id }
                        ) { yarn ->
                            YarnGridCard(
                                yarn = yarn,
                                onClick = { onNavigateToDetail(yarn.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YarnGridCard(
    yarn: YarnEntity,
    onClick: () -> Unit
) {
    val photoList = remember(yarn.photos) {
        try {
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<List<String>>(yarn.photos)
        } catch (_: Exception) {
            emptyList()
        }
    }

    val materialLabelRes = remember(yarn.material) {
        try {
            com.hooky.app.domain.model.enums.Material.valueOf(yarn.material).labelResId
        } catch (_: Exception) { null }
    }
    val materialDisplayName = materialLabelRes?.let { stringResource(it) } ?: yarn.material

    val weightLabelRes = remember(yarn.weightCategory) {
        yarn.weightCategory?.let {
            try {
                com.hooky.app.domain.model.enums.WeightCategory.valueOf(it).labelResId
            } catch (_: Exception) { null }
        }
    }
    val weightDisplayName = weightLabelRes?.let { stringResource(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column {
            // Photo area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photoList.isNotEmpty()) {
                    AsyncImage(
                        model = photoList.first(),
                        contentDescription = yarn.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_yarns),
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Card content
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Yarn name
                Text(
                    text = yarn.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                // Brand (if present)
                yarn.brand?.let { brand ->
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                // Color name with indicator circle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Real colour when known; a neutral grey dot means "no colour yet"
                    YarnColorDot(YarnColor.effective(yarn.colorHex, yarn.color) ?: 0x9CA3AF, size = 12.dp)
                    Text(
                        text = yarnColorLabel(yarn.color),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                // Material badge
                StatusBadge(text = materialDisplayName, style = BadgeStyle.MUTED)

                // Weight badge (if present)
                weightDisplayName?.let { weight ->
                    StatusBadge(text = weight, style = BadgeStyle.OUTLINE)
                }
            }
        }
    }
}

@Composable
private fun YarnEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yarns),
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.yarn_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.yarn_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
