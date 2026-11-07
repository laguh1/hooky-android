package com.hooky.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Brand purple for foreground use (text, icons, outlines): readable in light and dark mode. */
val BrandPurpleText: Color
    @Composable @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) BrandPurpleOnDark else BrandPurple
