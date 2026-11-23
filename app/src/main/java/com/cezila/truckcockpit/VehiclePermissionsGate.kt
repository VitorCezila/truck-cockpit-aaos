package com.cezila.truckcockpit

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat

/**
 * Runtime permissions for the vehicle data. CAR_POWERTRAIN and CAR_INFO are normal permissions,
 * granted at install time.
 */
private val RuntimeVehiclePermissions = arrayOf(
    "android.car.permission.CAR_SPEED",
    "android.car.permission.CAR_ENERGY",
)

/**
 * Asks for the vehicle permissions once, then shows [content] whatever the answer: a denied
 * permission only makes its values "not available". Waiting for the answer means the data
 * source subscribes after the permissions are settled.
 */
@Composable
fun VehiclePermissionsGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var answered by rememberSaveable { mutableStateOf(context.hasAllPermissions()) }
    // Saved across recreation: a second request while the dialog is up returns an empty result at once.
    var requested by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { answered = true }

    LaunchedEffect(Unit) {
        if (!answered && !requested) {
            requested = true
            launcher.launch(RuntimeVehiclePermissions)
        }
    }

    if (answered) {
        content()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.waiting_for_permissions),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Context.hasAllPermissions(): Boolean = RuntimeVehiclePermissions.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}
