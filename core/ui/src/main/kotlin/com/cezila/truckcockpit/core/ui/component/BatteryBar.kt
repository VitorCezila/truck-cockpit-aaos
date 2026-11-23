package com.cezila.truckcockpit.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Battery level as [segments] blocks, readable at a glance. Decorative: the percentage next to it
 * carries the value for accessibility.
 */
@Composable
fun BatteryBar(
    percent: Int,
    modifier: Modifier = Modifier,
    segments: Int = 10,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val filled = (percent.coerceIn(0, 100) / 100f * segments).roundToInt()
    Row(
        modifier = modifier
            .height(16.dp)
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(segments) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (index < filled) color else MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = MaterialTheme.shapes.extraSmall,
                    ),
            )
        }
    }
}
