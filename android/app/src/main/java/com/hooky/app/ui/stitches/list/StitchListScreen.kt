package com.hooky.app.ui.stitches.list

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hooky.app.R
import com.hooky.app.data.db.entity.IdeaEntity
import com.hooky.app.data.db.entity.StitchEntity
import com.hooky.app.ui.components.BadgeStyle
import com.hooky.app.ui.components.StatusBadge
import com.hooky.app.ui.stitches.ideas.IdeasUiState
import com.hooky.app.ui.stitches.ideas.IdeasViewModel
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White
import com.hooky.app.ui.util.labelResId

private enum class StitchTab { LIBRARY, IDEAS }

private data class PlatformInfo(
    val icon: ImageVector,
    val tint: Color,
    val actionLabel: String
)

private fun platformInfoFromUrl(url: String): PlatformInfo = when {
    "youtube.com" in url || "youtu.be" in url ->
        PlatformInfo(Icons.Filled.PlayCircleFilled, Color(0xFFCC0000), "Watch")
    "instagram.com" in url ->
        PlatformInfo(Icons.Filled.CameraAlt, Color(0xFFE1306C), "View")
    "tiktok.com" in url ->
        PlatformInfo(Icons.Filled.MusicNote, Color(0xFF010101), "View")
    else ->
        PlatformInfo(Icons.Filled.Language, Color(0xFF6B7280), "Open")
}

private fun sourceFromUrl(url: String): String = when {
    "youtube.com" in url || "youtu.be" in url -> "YouTube"
    "instagram.com" in url -> "Instagram"
    "tiktok.com" in url -> "TikTok"
    else -> "Web"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchListScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: StitchListViewModel = hiltViewModel(),
    ideasViewModel: IdeasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ideasUiState by ideasViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(StitchTab.LIBRARY) }

    val localeTag = remember {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) java.util.Locale.getDefault().toLanguageTag()
        else locales[0]?.toLanguageTag() ?: java.util.Locale.getDefault().toLanguageTag()
    }

    LaunchedEffect(localeTag) {
        ideasViewModel.load(localeTag)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(StitchListAction.ClearError)
        }
    }

    LaunchedEffect(ideasUiState.error) {
        ideasUiState.error?.let {
            snackbarHostState.showSnackbar(it)
            ideasViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.stitches_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
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
            when (selectedTab) {
                StitchTab.LIBRARY -> FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.stitch_new))
                }
                StitchTab.IDEAS -> FloatingActionButton(
                    onClick = ideasViewModel::showAddDialog,
                    containerColor = BrandPurple,
                    contentColor = White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.ideas_add_button))
                }
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
            StitchTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            when (selectedTab) {
                StitchTab.LIBRARY -> LibraryContent(
                    uiState = uiState,
                    onNavigateToDetail = onNavigateToDetail,
                    onAction = viewModel::onAction
                )
                StitchTab.IDEAS -> IdeasContent(
                    ideasUiState = ideasUiState,
                    onDelete = ideasViewModel::deleteIdea
                )
            }
        }

        if (ideasUiState.showAddDialog) {
            AddIdeaDialog(
                onDismiss = ideasViewModel::dismissAddDialog,
                onSave = { title, url, description ->
                    ideasViewModel.addIdea(
                        title = title,
                        source = sourceFromUrl(url),
                        url = url,
                        description = description,
                        locale = localeTag
                    )
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tab toggle
// ---------------------------------------------------------------------------

@Composable
private fun StitchTabRow(
    selectedTab: StitchTab,
    onTabSelected: (StitchTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    StitchTab.LIBRARY to stringResource(R.string.stitch_tab_library),
                    StitchTab.IDEAS to stringResource(R.string.stitch_tab_suggestions)
                ).forEach { (tab, label) ->
                    val selected = selectedTab == tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) BrandPurple else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(40.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (selected) BrandPurple else Color.Transparent)
                        )
                    }
                }
            }
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        }
    }
}

// ---------------------------------------------------------------------------
// Library tab (existing content)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryContent(
    uiState: StitchListUiState,
    onNavigateToDetail: (Int) -> Unit,
    onAction: (StitchListAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { onAction(StitchListAction.SearchQueryChanged(it)) },
            placeholder = {
                Text(stringResource(R.string.stitch_search_hint), color = TextMuted)
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted)
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
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            StitchFilter.values().forEach { filter ->
                val selected = uiState.selectedFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { onAction(StitchListAction.FilterSelected(filter)) },
                    label = {
                        Text(
                            text = stringResource(filter.labelResId),
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
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.stitches.isEmpty() -> StitchEmptyState()
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = uiState.stitches, key = { it.id }) { stitch ->
                        StitchGridCard(
                            stitch = stitch,
                            onClick = { onNavigateToDetail(stitch.id) }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Ideas tab
// ---------------------------------------------------------------------------

@Composable
private fun IdeasContent(
    ideasUiState: IdeasUiState,
    onDelete: (Int) -> Unit
) {
    when {
        ideasUiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPurple)
            }
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = stringResource(R.string.stitch_suggestions_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(items = ideasUiState.ideas, key = { it.id }) { idea ->
                    IdeaCard(idea = idea, onDelete = { onDelete(idea.id) })
                }
            }
        }
    }
}

@Composable
private fun IdeaCard(
    idea: IdeaEntity,
    onDelete: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val platform = remember(idea.url) { platformInfoFromUrl(idea.url) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = platform.icon,
                contentDescription = null,
                tint = platform.tint,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = idea.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = idea.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1
                )
                idea.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { uriHandler.openUri(idea.url) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPurple,
                        contentColor = White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = platform.actionLabel,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.idea_delete),
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add idea dialog
// ---------------------------------------------------------------------------

@Composable
private fun AddIdeaDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, description: String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    val isSaveEnabled = title.isNotBlank() && url.isNotBlank()
    val detectedPlatform = remember(url) { sourceFromUrl(url) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.ideas_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.ideas_dialog_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple)
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.ideas_dialog_url_label)) },
                    singleLine = true,
                    placeholder = { Text("https://", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple),
                    supportingText = if (url.isNotBlank()) {
                        { Text(detectedPlatform, color = TextMuted) }
                    } else null
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.ideas_dialog_description_label)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title.trim(),
                        url.trim(),
                        description.trim().takeIf { it.isNotEmpty() }
                    )
                },
                enabled = isSaveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = White)
            ) {
                Text(stringResource(R.string.ideas_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ideas_dialog_cancel))
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Library composables
// ---------------------------------------------------------------------------

@Composable
private fun StitchGridCard(
    stitch: StitchEntity,
    onClick: () -> Unit
) {
    val photoList = remember(stitch.photos) {
        try {
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<List<String>>(stitch.photos)
        } catch (_: Exception) {
            emptyList()
        }
    }

    val categoryResId = remember(stitch.category) {
        stitch.category?.let { try { com.hooky.app.domain.model.enums.StitchCategory.valueOf(it).labelResId } catch (_: Exception) { null } }
    }
    val categoryDisplayName = categoryResId?.let { stringResource(it) } ?: stitch.category

    val difficultyResId = remember(stitch.difficulty) {
        stitch.difficulty?.let { try { com.hooky.app.domain.model.enums.Difficulty.valueOf(it).labelResId } catch (_: Exception) { null } }
    }
    val difficultyDisplayName = difficultyResId?.let { stringResource(it) } ?: stitch.difficulty

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
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
                        contentDescription = stitch.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_stitches),
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stitch.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                stitch.abbreviation?.let { abbr ->
                    Text(
                        text = abbr,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                categoryDisplayName?.let { cat ->
                    StatusBadge(text = cat, style = BadgeStyle.MUTED)
                }

                difficultyDisplayName?.let { diff ->
                    StatusBadge(text = diff, style = BadgeStyle.OUTLINE)
                }
            }
        }
    }
}

@Composable
private fun StitchEmptyState() {
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
                    painter = painterResource(R.drawable.ic_stitches),
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.stitch_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.stitch_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
