package com.cezila.truckcockpit.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.cezila.truckcockpit.core.ui.R

/**
 * A big number with its unit on the same baseline, e.g. "296 km".
 * A null [value] shows "Not available" in the unit style instead.
 */
@Composable
fun ValueWithUnit(
    value: String?,
    unit: String,
    valueStyle: TextStyle,
    unitStyle: TextStyle,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    if (value == null) {
        Text(
            text = stringResource(R.string.core_ui_not_available),
            style = unitStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    val description = "$value $unit"
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            modifier = Modifier.alignByBaseline(),
        )
        Text(
            text = unit,
            style = unitStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alignByBaseline(),
        )
    }
}
