package com.cezila.truckcockpit.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Minimum height of every touch target in the app, above the 76dp automotive guideline. */
val MinTouchTarget = 88.dp

/** A primary action, sized for a gloved hand: at least [MinTouchTarget] tall, accent filled. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinTouchTarget),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 16.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

/** A secondary action, same size as [BigButton], outlined. */
@Composable
fun BigOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinTouchTarget),
        shape = MaterialTheme.shapes.small,
        border = ButtonDefaults.outlinedButtonBorder().copy(width = 2.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 16.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}
