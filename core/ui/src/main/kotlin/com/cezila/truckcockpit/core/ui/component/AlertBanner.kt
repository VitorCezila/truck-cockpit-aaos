package com.cezila.truckcockpit.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

enum class AlertSeverity { Ok, Warning, Critical }

/**
 * A status message with an accent stripe on the left, e.g. "Battery won't reach destination".
 * The color comes from [severity]; the text says the same thing, so color is never the only cue.
 */
@Composable
fun AlertBanner(
    title: String,
    severity: AlertSeverity,
    modifier: Modifier = Modifier,
    message: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    messageStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    val accent = severity.accent()
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Only the title is a live region: it changes with the status, while the message
                // (e.g. "28 km short") changes often and would be read out again each time.
                Text(
                    text = title,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                if (message != null) {
                    Text(text = message, style = messageStyle, color = accent)
                }
            }
        }
    }
}

@Composable
private fun AlertSeverity.accent(): Color = when (this) {
    AlertSeverity.Ok -> MaterialTheme.colorScheme.primary
    AlertSeverity.Warning -> MaterialTheme.colorScheme.secondary
    AlertSeverity.Critical -> MaterialTheme.colorScheme.error
}
