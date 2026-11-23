package com.cezila.truckcockpit.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.ui.R

/**
 * The selected gear in a square: "P", "R", "N", "D" or a gear number.
 * Park is filled with the accent color; any other gear is outlined, with the letter in the accent color.
 * A null [gear] shows the placeholder.
 */
@Composable
fun GearIndicator(
    gear: String?,
    filled: Boolean,
    size: Dp,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.secondary
    val description = if (gear != null) {
        stringResource(R.string.core_ui_gear_description, gear)
    } else {
        stringResource(R.string.core_ui_gear_description, stringResource(R.string.core_ui_not_available))
    }
    Surface(
        modifier = modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = description },
        shape = MaterialTheme.shapes.extraSmall,
        color = if (filled && gear != null) accent else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (filled && gear != null) null else BorderStroke(size / 64, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = gear ?: stringResource(R.string.core_ui_value_placeholder),
                style = textStyle,
                color = when {
                    gear == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    filled -> MaterialTheme.colorScheme.onSecondary
                    else -> accent
                },
            )
        }
    }
}

/** The letter or number shown in a [GearIndicator]. */
fun Gear.shortLabel(): String = when (this) {
    Gear.Park -> "P"
    Gear.Reverse -> "R"
    Gear.Neutral -> "N"
    Gear.Drive -> "D"
    is Gear.Manual -> number.toString()
}
