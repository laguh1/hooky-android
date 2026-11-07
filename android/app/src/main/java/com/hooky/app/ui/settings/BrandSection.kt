package com.hooky.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hooky.app.R
import com.hooky.app.ui.theme.BrandPurple
import com.hooky.app.ui.theme.BrandPurpleText
import com.hooky.app.ui.theme.TextMuted
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.util.MakerBrand
import java.io.File

/**
 * "Your brand" in Settings: shop name / @handle and logo for share cards (free), and the
 * Pro switch to hide the small "made with Hooky App" mark.
 */
@Composable
fun BrandSection(isPremium: Boolean, onNavigateToPro: () -> Unit) {
    val context = LocalContext.current
    var brand by remember { mutableStateOf(MakerBrand.load(context)) }
    // Bumped after a new logo is saved, so the preview reloads the same file path
    var logoVersion by remember { mutableStateOf(0) }
    var editingName by remember { mutableStateOf(false) }

    val pickLogo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && MakerBrand.saveLogo(context, uri)) {
            brand = MakerBrand.load(context); logoVersion++
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Column {
            BrandRow(
                title = stringResource(R.string.settings_brand_name_title),
                subtitle = brand.name.ifBlank { stringResource(R.string.settings_brand_name_sub_empty) },
                leading = { Icon(Icons.Filled.Storefront, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                onClick = { editingName = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            BrandRow(
                title = stringResource(R.string.settings_brand_logo_title),
                subtitle = stringResource(
                    if (brand.logoPath == null) R.string.settings_brand_logo_sub_empty else R.string.settings_brand_logo_sub_set
                ),
                leading = {
                    val path = brand.logoPath
                    if (path != null) {
                        // White tile: logos are often dark or transparent; the card uses the same tile
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(androidx.compose.ui.graphics.Color.White).padding(3.dp)) {
                            AsyncImage(
                                model = coil.request.ImageRequest.Builder(context).data(File(path))
                                    .memoryCacheKey("brand-logo-$logoVersion").diskCacheKey("brand-logo-$logoVersion").build(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else {
                        Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                },
                trailing = if (brand.logoPath != null) ({
                    IconButton(onClick = { MakerBrand.removeLogo(context); brand = MakerBrand.load(context) }) {
                        Icon(Icons.Filled.Close, stringResource(R.string.settings_brand_logo_remove), tint = TextMuted)
                    }
                }) else null,
                onClick = { pickLogo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            // "Made with Hooky App" mark: always on for free; Pro can switch it off
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!isPremium) onNavigateToPro() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_brand_mark_title), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        stringResource(if (isPremium) R.string.settings_brand_mark_sub_pro else R.string.settings_brand_mark_sub_free),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPremium) TextSecondary else BrandPurpleText
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = !(isPremium && brand.hideHookyMark),
                    enabled = isPremium,
                    onCheckedChange = { show -> MakerBrand.setHideHookyMark(context, !show); brand = MakerBrand.load(context) }
                )
            }
        }
    }

    if (editingName) {
        var text by remember { mutableStateOf(brand.name) }
        val focus = remember { androidx.compose.ui.focus.FocusRequester() }
        androidx.compose.runtime.LaunchedEffect(Unit) { focus.requestFocus() }
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text(stringResource(R.string.settings_brand_name_title)) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 40) text = it },
                    placeholder = { Text(stringResource(R.string.settings_brand_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    MakerBrand.saveName(context, text); brand = MakerBrand.load(context); editingName = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { editingName = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun BrandRow(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { leading() }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        trailing?.invoke()
    }
}
