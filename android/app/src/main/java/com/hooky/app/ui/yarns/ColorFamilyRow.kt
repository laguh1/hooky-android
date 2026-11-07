package com.hooky.app.ui.yarns

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hooky.app.R
import com.hooky.app.domain.model.ColorFamily
import com.hooky.app.ui.theme.BorderLight
import com.hooky.app.ui.theme.Slate
import com.hooky.app.ui.theme.TextSecondary
import com.hooky.app.ui.theme.White

@Composable
fun ColorFamily.label(): String = stringResource(
    when (this) {
        ColorFamily.RED -> R.string.color_family_red
        ColorFamily.PINK -> R.string.color_family_pink
        ColorFamily.ORANGE -> R.string.color_family_orange
        ColorFamily.YELLOW -> R.string.color_family_yellow
        ColorFamily.GREEN -> R.string.color_family_green
        ColorFamily.BLUE -> R.string.color_family_blue
        ColorFamily.PURPLE -> R.string.color_family_purple
        ColorFamily.BROWN -> R.string.color_family_brown
        ColorFamily.NEUTRAL -> R.string.color_family_neutral
    }
)

/**
 * "By colour" sort chip (optional) followed by one circle per colour group present.
 * Tapping a circle filters to that group; tapping it again clears the filter.
 */
@Composable
fun ColorFamilyRow(
    families: List<ColorFamily>,
    selected: ColorFamily?,
    onSelected: (ColorFamily?) -> Unit,
    sortByColor: Boolean? = null,
    onToggleSort: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    // Fade the right edge while more circles are off-screen, so it's clear the row scrolls.
    // Drawn as an alpha mask, so it works on any background (light, dark, grey).
    val fadeWidth = with(LocalDensity.current) { 40.dp.toPx() }
    Row(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                if (scroll.canScrollForward) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startX = size.width - fadeWidth,
                            endX = size.width
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
            }
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (sortByColor != null) {
            FilterChip(
                selected = sortByColor,
                onClick = onToggleSort,
                label = { Text(stringResource(R.string.sort_by_colour), style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                leadingIcon = { Icon(Icons.Filled.Palette, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = Color.Transparent,
                    labelColor = TextSecondary,
                    iconColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = sortByColor,
                    borderColor = BorderLight,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
        families.forEach { family ->
            val isSelected = family == selected
            val cd = stringResource(R.string.color_filter_cd, family.label())
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(if (isSelected) 2.dp else 0.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                    .padding(5.dp)
                    .clickable(onClickLabel = cd) { onSelected(if (isSelected) null else family) },
                contentAlignment = Alignment.Center
            ) {
                YarnColorDot(family.swatch, size = 28.dp)
            }
        }
    }
}
