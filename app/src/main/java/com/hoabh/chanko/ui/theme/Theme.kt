package com.hoabh.chanko.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Approved brand colors; neutral containers/on-colors are Reference, not approved PV-01 tokens.
private val LightColorScheme = lightColorScheme(
    primary = CandyColors.primary, onPrimary = CandyColors.ink,
    primaryContainer = CandyColors.primary, onPrimaryContainer = CandyColors.ink,
    inversePrimary = CandyColors.primary,
    secondary = CandyColors.secondary, onSecondary = CandyColors.ink,
    secondaryContainer = CandyColors.secondary, onSecondaryContainer = CandyColors.ink,
    tertiary = CandyColors.mint, onTertiary = CandyColors.ink,
    tertiaryContainer = CandyColors.mint, onTertiaryContainer = CandyColors.ink,
    background = CandyColors.background, onBackground = CandyColors.ink,
    surface = Color.White, onSurface = CandyColors.ink,
    surfaceVariant = CandyColors.background, onSurfaceVariant = CandyColors.mutedInk,
    surfaceTint = Color.Transparent,
    surfaceDim = CandyColors.background, surfaceBright = Color.White,
    surfaceContainerLowest = Color.White, surfaceContainerLow = CandyColors.background,
    surfaceContainer = CandyColors.background, surfaceContainerHigh = CandyColors.background,
    surfaceContainerHighest = CandyColors.background,
    outline = CandyColors.outline, outlineVariant = CandyColors.outline,
    error = CandyColors.errorText, onError = Color.White,
    errorContainer = CandyColors.error, onErrorContainer = CandyColors.ink,
    inverseSurface = CandyColors.ink, inverseOnSurface = Color.White
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun ChankoTheme(
    // Compatibility parameters: MVP always renders the approved Light scheme.
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = LightColorScheme, typography = ChankoTypography,
        shapes = ChankoShapes, content = content)
}
