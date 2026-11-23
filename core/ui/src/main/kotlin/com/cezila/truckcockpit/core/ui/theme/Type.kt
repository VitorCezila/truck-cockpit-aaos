package com.cezila.truckcockpit.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun style(size: TextUnit, lineHeight: TextUnit, weight: FontWeight) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
)

/**
 * All 15 Material styles, scaled for a 1920x1080 head unit read at arm's length.
 * Nothing below 24sp, so default Material components stay glanceable.
 */
val TruckCockpitTypography = Typography(
    displayLarge = style(120.sp, 128.sp, FontWeight.Bold),
    displayMedium = style(96.sp, 104.sp, FontWeight.Bold),
    displaySmall = style(72.sp, 80.sp, FontWeight.Bold),
    headlineLarge = style(56.sp, 64.sp, FontWeight.SemiBold),
    headlineMedium = style(48.sp, 56.sp, FontWeight.SemiBold),
    headlineSmall = style(40.sp, 48.sp, FontWeight.SemiBold),
    titleLarge = style(36.sp, 44.sp, FontWeight.SemiBold),
    titleMedium = style(32.sp, 40.sp, FontWeight.SemiBold),
    titleSmall = style(28.sp, 36.sp, FontWeight.Medium),
    bodyLarge = style(32.sp, 40.sp, FontWeight.Normal),
    bodyMedium = style(28.sp, 36.sp, FontWeight.Normal),
    bodySmall = style(24.sp, 32.sp, FontWeight.Normal),
    labelLarge = style(28.sp, 36.sp, FontWeight.Medium),
    labelMedium = style(26.sp, 32.sp, FontWeight.Medium),
    labelSmall = style(24.sp, 32.sp, FontWeight.Medium),
)
