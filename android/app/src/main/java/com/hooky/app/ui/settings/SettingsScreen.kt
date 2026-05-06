package com.hooky.app.ui.settings

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
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.hooky.app.ui.theme.BackgroundLight
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary

private data class LanguageOption(val tag: String, val label: String, val sublabel: String)

private val LANGUAGES = listOf(
    LanguageOption("", "System default", "Follows device language"),
    LanguageOption("en", "English", "English"),
    LanguageOption("es", "Spanish", "Español"),
    LanguageOption("pt-BR", "Portuguese (Brazil)", "Português (BR)"),
)

private fun currentLanguageTag(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty) "" else locales[0]?.toLanguageTag() ?: ""
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTag by remember { mutableStateOf(currentLanguageTag()) }

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
                        contentDescription = "Back",
                        tint = Slate
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LANGUAGE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = androidx.compose.ui.unit.TextUnit(
                        1.5f,
                        androidx.compose.ui.unit.TextUnitType.Sp
                    )
                ),
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BackgroundLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    LANGUAGES.forEachIndexed { index, option ->
                        LanguageRow(
                            option = option,
                            selected = option.tag == selectedTag,
                            onClick = { selectLanguage(option.tag) }
                        )
                        if (index < LANGUAGES.lastIndex) {
                            Divider(
                                color = BorderLight,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    option: LanguageOption,
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
                text = option.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Slate else MaterialTheme.colorScheme.onSurface
            )
            if (option.sublabel.isNotEmpty() && option.tag.isNotEmpty()) {
                Text(
                    text = option.sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Icon(
            imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) Slate else TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
