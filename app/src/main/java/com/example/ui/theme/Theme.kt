package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MidnightChronicleColorScheme = darkColorScheme(
    primary = BurnishedGold,
    onPrimary = ObsidianBg,
    primaryContainer = BurnishedGoldDark,
    onPrimaryContainer = BurnishedGold,
    secondary = DeepTealAccent,
    onSecondary = ObsidianBg,
    secondaryContainer = DeepTealContainer,
    onSecondaryContainer = DeepTealAccent,
    tertiary = RoyalIndigo,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = WarmIvory,
    surface = SlateSurface,
    onSurface = WarmIvory,
    surfaceVariant = SlateSurfaceElevated,
    onSurfaceVariant = MutedParchment,
    outline = SlateBorder,
    error = CrimsonLoss,
    onError = ObsidianBg
)

private val WarmVellumColorScheme = lightColorScheme(
    primary = BronzePrimary,
    onPrimary = VellumSurface,
    primaryContainer = BronzeContainer,
    onPrimaryContainer = InkCharcoal,
    secondary = DeepTealContainer,
    onSecondary = VellumSurface,
    secondaryContainer = DeepTealAccent.copy(alpha = 0.18f),
    onSecondaryContainer = DeepTealContainer,
    tertiary = RoyalIndigo,
    onTertiary = VellumSurface,
    background = VellumBg,
    onBackground = InkCharcoal,
    surface = VellumSurface,
    onSurface = InkCharcoal,
    surfaceVariant = VellumSurfaceElevated,
    onSurfaceVariant = SlateMutedLight,
    outline = VellumBorder,
    error = CrimsonLoss,
    onError = VellumSurface
)

val EpochShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EpochLifeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MidnightChronicleColorScheme else WarmVellumColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = EpochTypography,
        shapes = EpochShapes,
        content = content
    )
}
