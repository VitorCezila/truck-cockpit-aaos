package com.cezila.truckcockpit.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Every slot is set so no Material baseline (purple) color leaks into components.
private val TruckCockpitColorScheme = darkColorScheme(
    primary = TruckCockpitColors.Healthy,
    onPrimary = TruckCockpitColors.OnHealthy,
    primaryContainer = TruckCockpitColors.HealthyContainer,
    onPrimaryContainer = TruckCockpitColors.OnHealthyContainer,
    inversePrimary = TruckCockpitColors.HealthyContainer,
    secondary = TruckCockpitColors.Warning,
    onSecondary = TruckCockpitColors.OnWarning,
    secondaryContainer = TruckCockpitColors.SurfaceHighest,
    onSecondaryContainer = TruckCockpitColors.OnBackground,
    tertiary = TruckCockpitColors.Warning,
    onTertiary = TruckCockpitColors.OnWarning,
    tertiaryContainer = TruckCockpitColors.WarningContainer,
    onTertiaryContainer = TruckCockpitColors.OnWarningContainer,
    error = TruckCockpitColors.Critical,
    onError = TruckCockpitColors.OnCritical,
    errorContainer = TruckCockpitColors.CriticalContainer,
    onErrorContainer = TruckCockpitColors.OnCriticalContainer,
    background = TruckCockpitColors.Background,
    onBackground = TruckCockpitColors.OnBackground,
    surface = TruckCockpitColors.Surface,
    onSurface = TruckCockpitColors.OnBackground,
    surfaceVariant = TruckCockpitColors.SurfaceVariant,
    onSurfaceVariant = TruckCockpitColors.OnSurfaceVariant,
    surfaceTint = TruckCockpitColors.Surface,
    inverseSurface = TruckCockpitColors.OnBackground,
    inverseOnSurface = TruckCockpitColors.Background,
    outline = TruckCockpitColors.Outline,
    outlineVariant = TruckCockpitColors.OutlineVariant,
    scrim = TruckCockpitColors.Background,
    surfaceBright = TruckCockpitColors.SurfaceHighest,
    surfaceDim = TruckCockpitColors.Background,
    surfaceContainerLowest = TruckCockpitColors.Background,
    surfaceContainerLow = TruckCockpitColors.Surface,
    surfaceContainer = TruckCockpitColors.Surface,
    surfaceContainerHigh = TruckCockpitColors.SurfaceHigh,
    surfaceContainerHighest = TruckCockpitColors.SurfaceHighest,
)

/** Always dark: the head unit is used day and night and a dark UI distracts less. No dynamic color. */
@Composable
fun TruckCockpitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TruckCockpitColorScheme,
        typography = TruckCockpitTypography,
        content = content,
    )
}
