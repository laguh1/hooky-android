package com.hooky.app.ui.needles.list

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hooky.app.data.db.entity.NeedleEntity
import com.hooky.app.domain.model.enums.NeedleType
import androidx.compose.ui.res.painterResource
import com.hooky.app.R
import com.hooky.app.ui.components.EmptyState
import com.hooky.app.ui.theme.BackgroundLight
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedleListScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: NeedleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(NeedleListAction.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Needles & Hooks", fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add needle")
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
                onValueChange = { viewModel.onAction(NeedleListAction.SearchQueryChanged(it)) },
                placeholder = { Text("Search needles…", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Slate,
                    unfocusedBorderColor = BorderLight,
                    cursorColor = Slate
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(NeedleFilter.values()) { filter ->
                    val selected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onAction(NeedleListAction.FilterSelected(filter)) },
                        label = {
                            Text(
                                text = filter.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate,
                            selectedLabelColor = White,
                            containerColor = BackgroundLight,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = BorderLight,
                            selectedBorderColor = Slate
                        )
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Slate)
                    }
                }
                uiState.needles.isEmpty() -> {
                    EmptyState(
                        icon = painterResource(R.drawable.ic_needles),
                        title = "No needles yet",
                        subtitle = "Add your first needle or hook",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.needles, key = { it.id }) { needle ->
                            NeedleListItem(
                                needle = needle,
                                onClick = { onNavigateToDetail(needle.id) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NeedleListItem(
    needle: NeedleEntity,
    onClick: () -> Unit
) {
    val typeLabel = try {
        NeedleType.valueOf(needle.type).displayName
    } catch (_: Exception) {
        needle.type
    }

    val sizeText = buildString {
        needle.sizeMm?.let { append("${it}mm") }
        needle.sizeLabel?.let {
            if (needle.sizeMm != null) append(" · ")
            append(it)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_needles),
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = needle.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    if (sizeText.isNotBlank()) {
                        Text("·", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(
                            text = sizeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                if (!needle.brand.isNullOrBlank()) {
                    Text(
                        text = needle.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if ((needle.quantity ?: 0) > 1) {
                Text(
                    text = "×${needle.quantity}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
        }
    }
}
