package com.cezila.truckcockpit.core.ui.theme

import androidx.compose.ui.graphics.Color

/** Truck Cockpit palette: near-black background, high contrast, status colors for battery and alerts. */
object TruckCockpitColors {
    val Background = Color(0xFF0E1113)
    val Surface = Color(0xFF1A1F23)
    val SurfaceVariant = Color(0xFF252B30)
    val SurfaceHigh = Color(0xFF2C3338)
    val SurfaceHighest = Color(0xFF343C42)
    val OnBackground = Color(0xFFF2F4F5)
    val OnSurfaceVariant = Color(0xFFB4BCC2)

    /** Control borders: at least 3:1 against [Surface] (WCAG 1.4.11). */
    val Outline = Color(0xFF7A848B)

    /** Decorative dividers only. */
    val OutlineVariant = Color(0xFF3A4248)

    /** Healthy values (battery OK, destination reachable). */
    val Healthy = Color(0xFF3DDC84)
    val OnHealthy = Color(0xFF00210F)
    val HealthyContainer = Color(0xFF0F3D24)
    val OnHealthyContainer = Color(0xFFB8F5CF)

    /** Warnings (low battery, tight range). */
    val Warning = Color(0xFFFFB020)
    val OnWarning = Color(0xFF2A1A00)
    val WarningContainer = Color(0xFF4A3300)
    val OnWarningContainer = Color(0xFFFFDDA6)

    /** Critical (destination unreachable). */
    val Critical = Color(0xFFFF4D4F)
    val OnCritical = Color(0xFF2D0001)
    val CriticalContainer = Color(0xFF5C1214)
    val OnCriticalContainer = Color(0xFFFFDAD8)
}
