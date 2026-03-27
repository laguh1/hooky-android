package com.crochet.manager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Slate,
    onPrimary = White,
    primaryContainer = BorderLight,
    onPrimaryContainer = SlateDark,
    secondary = TextSecondary,
    onSecondary = White,
    secondaryContainer = SurfaceLight,
    onSecondaryContainer = TextPrimary,
    tertiary = TextMuted,
    onTertiary = White,
    tertiaryContainer = BackgroundLight,
    onTertiaryContainer = TextPrimary,
    error = ErrorRed,
    onError = White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = BorderStrong,
    inverseSurface = Slate,
    inverseOnSurface = White,
    inversePrimary = DarkSlate,
    surfaceTint = Slate,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkSlate,
    onPrimary = SlateDark,
    primaryContainer = DarkBorder,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkTextSecondary,
    onSecondary = SlateDark,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkTextSecondary,
    onTertiary = SlateDark,
    tertiaryContainer = DarkBackground,
    onTertiaryContainer = DarkTextPrimary,
    error = ErrorRed,
    onError = White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkSurface,
    inverseSurface = DarkSlate,
    inverseOnSurface = SlateDark,
    inversePrimary = Slate,
    surfaceTint = DarkSlate,
)

@Composable
fun CrochetManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
