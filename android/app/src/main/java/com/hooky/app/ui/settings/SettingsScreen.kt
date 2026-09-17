package com.hooky.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Switch
import androidx.compose.ui.res.stringResource
import com.hooky.app.BuildConfig
import com.hooky.app.R
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private const val PREFS_NAME = "hooky_settings"
private const val KEY_NIGHT_MODE = "night_mode"

// endonym is the language's own name for itself (e.g. "Español") — deliberately
// shown as-is regardless of app language, not translated.
private data class LanguageOption(val tag: String, val labelRes: Int, val endonym: String)
private data class ThemeOption(val mode: Int, val labelRes: Int, val sublabelRes: Int)
// CurrencyOption and CURRENCIES defined in CurrencyPrefs.kt

private val LANGUAGES = listOf(
    LanguageOption("", R.string.settings_language_system_label, ""),
    LanguageOption("en", R.string.settings_lang_english, "English"),
    LanguageOption("es", R.string.settings_lang_spanish, "Español"),
    LanguageOption("pt-BR", R.string.settings_lang_portuguese, "Português (BR)"),
)

private val THEMES = listOf(
    ThemeOption(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.settings_theme_system_label, R.string.settings_theme_system_sublabel),
    ThemeOption(AppCompatDelegate.MODE_NIGHT_NO, R.string.settings_theme_light_label, R.string.settings_theme_light_sublabel),
    ThemeOption(AppCompatDelegate.MODE_NIGHT_YES, R.string.settings_theme_dark_label, R.string.settings_theme_dark_sublabel),
)

private fun currentLanguageTag(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty) "" else locales[0]?.toLanguageTag() ?: ""
}

private fun getSavedNightMode(context: Context): Int =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

private fun saveAndApplyNightMode(context: Context, mode: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_NIGHT_MODE, mode)
        .apply()
    AppCompatDelegate.setDefaultNightMode(mode)
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.isPremium.collectAsState()
    val installationId by viewModel.installationId.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val backupState by viewModel.backupState.collectAsState()

    var selectedTag by remember { mutableStateOf(currentLanguageTag()) }
    var selectedNightMode by remember { mutableStateOf(getSavedNightMode(context)) }
    var selectedCurrency by remember { mutableStateOf(getCurrentCurrencyCode(context)) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.import(it) }
    }

    LaunchedEffect(backupState) {
        if (backupState is BackupState.ExportReady) {
            val uri = (backupState as BackupState.ExportReady).uri
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.settings_backup_share_title)))
            viewModel.clearBackupState()
        }
    }

    fun selectLanguage(tag: String) {
        if (tag == selectedTag) return
        selectedTag = tag
        val localeList = if (tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun selectTheme(mode: Int) {
        if (mode == selectedNightMode) return
        selectedNightMode = mode
        saveAndApplyNightMode(context, mode)
    }

    fun selectCurrency(code: String) {
        if (code == selectedCurrency) return
        selectedCurrency = code
        saveCurrency(context, code)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.action_settings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Appearance section
            SectionHeader(stringResource(R.string.settings_section_appearance))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    THEMES.forEachIndexed { index, option ->
                        OptionRow(
                            label = stringResource(option.labelRes),
                            sublabel = stringResource(option.sublabelRes),
                            selected = option.mode == selectedNightMode,
                            onClick = { selectTheme(option.mode) }
                        )
                        if (index < THEMES.lastIndex) {
                            Divider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Language section
            SectionHeader(stringResource(R.string.settings_section_language))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    LANGUAGES.forEachIndexed { index, option ->
                        OptionRow(
                            label = stringResource(option.labelRes),
                            sublabel = if (option.tag.isNotEmpty()) option.endonym else "",
                            selected = option.tag == selectedTag,
                            onClick = { selectLanguage(option.tag) }
                        )
                        if (index < LANGUAGES.lastIndex) {
                            Divider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            if (selectedTag.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.settings_voice_lang_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Currency section
            SectionHeader(stringResource(R.string.settings_section_currency))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    CURRENCIES.forEachIndexed { index, option ->
                        OptionRow(
                            label = stringResource(option.labelRes),
                            sublabel = option.sublabel,
                            selected = option.code == selectedCurrency,
                            onClick = { selectCurrency(option.code) }
                        )
                        if (index < CURRENCIES.lastIndex) {
                            Divider(
                                color = MaterialTheme.colorScheme.outline,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Backup section
            SectionHeader(stringResource(R.string.settings_section_backup))

            val isBusy = backupState is BackupState.Exporting || backupState is BackupState.Importing

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isBusy) { viewModel.export() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_backup_export_title),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_backup_export_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        if (backupState is BackupState.Exporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isBusy) { importLauncher.launch("*/*") }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_backup_import_title),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_backup_import_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        if (backupState is BackupState.Importing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Restore,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            when (val s = backupState) {
                is BackupState.ImportDone -> {
                    LaunchedEffect(s) {
                        delay(4000)
                        viewModel.clearBackupState()
                    }
                    Text(
                        text = stringResource(R.string.settings_backup_import_done, s.count),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                is BackupState.Error -> {
                    LaunchedEffect(s) {
                        delay(4000)
                        viewModel.clearBackupState()
                    }
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help section
            SectionHeader(stringResource(R.string.settings_section_help))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToHelp() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HelpOutline,
                            contentDescription = null,
                            tint = Slate,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.settings_help_title),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_help_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beta / Premium section
            SectionHeader(stringResource(R.string.settings_section_beta))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    // Status row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_beta_access_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPremium) stringResource(R.string.settings_beta_access_premium) else stringResource(R.string.settings_beta_access_standard),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isPremium) BrandPurple else TextSecondary
                            )
                        }
                        if (isPremium) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (installationId.isNotBlank()) {
                        Divider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        // Tester ID row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_beta_tester_id_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = installationId,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(installationId)) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = stringResource(R.string.settings_beta_copy_tester_id_cd),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.settings_section_debug))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_debug_simulate_premium),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.settings_debug_simulate_premium_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = isPremium,
                            onCheckedChange = { viewModel.setDebugPremium(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = androidx.compose.ui.unit.TextUnit(
                1.5f,
                androidx.compose.ui.unit.TextUnitType.Sp
            )
        ),
        color = TextMuted,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun OptionRow(
    label: String,
    sublabel: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (sublabel.isNotEmpty()) {
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Icon(
            imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
