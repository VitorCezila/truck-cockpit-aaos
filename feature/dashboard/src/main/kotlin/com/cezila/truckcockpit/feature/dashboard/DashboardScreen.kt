package com.cezila.truckcockpit.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.valueOrNull
import com.cezila.truckcockpit.core.ui.component.BatteryBar
import com.cezila.truckcockpit.core.ui.component.GearIndicator
import com.cezila.truckcockpit.core.ui.component.MetricCard
import com.cezila.truckcockpit.core.ui.component.StatusIndicator
import com.cezila.truckcockpit.core.ui.component.ValueWithUnit
import com.cezila.truckcockpit.core.ui.component.shortLabel
import com.cezila.truckcockpit.core.ui.theme.TruckCockpitTheme

/** The only composable that sees the ViewModel. */
@Composable
fun DashboardRoute(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // No effects yet; handle them here when navigation arrives.
            viewModel.effects.collect { }
        }
    }
    DashboardScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** Stateless: full layout when parked, reduced layout while driving. */
@Composable
fun DashboardScreen(
    state: DashboardState,
    onIntent: (DashboardIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (state.drivingMode) {
            DrivingMode.Parked -> DashboardParkedContent(state)
            DrivingMode.Driving -> DashboardDrivingContent(state)
        }
    }
}

/** Full layout: speed and gear on the left, battery and range on the right. */
@Composable
private fun DashboardParkedContent(state: DashboardState) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(CardSpacing),
    ) {
        MetricCard(
            title = null,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            StatusIndicator(
                label = stringResource(R.string.dashboard_status_parked),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ValueWithUnit(
                    value = state.speedKmh.valueOrNull()?.toString(),
                    unit = stringResource(R.string.dashboard_unit_kmh),
                    valueStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 160.sp, lineHeight = 168.sp),
                    unitStyle = MaterialTheme.typography.headlineSmall,
                )
                GearIndicator(
                    gear = state.gear.valueOrNull()?.shortLabel(),
                    filled = state.gear.valueOrNull() == Gear.Park,
                    size = 120.dp,
                    textStyle = MaterialTheme.typography.displaySmall,
                )
            }
            Text(
                text = stringResource(R.string.dashboard_speed),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(CardSpacing),
        ) {
            BatteryCard(state, Modifier.weight(1f).fillMaxWidth())
            RangeCard(
                rangeKm = state.rangeKm,
                title = stringResource(R.string.dashboard_range),
                valueStyle = MaterialTheme.typography.displaySmall,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

/** Reduced layout: only speed, gear and range, larger. */
@Composable
private fun DashboardDrivingContent(state: DashboardState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(ScreenPadding),
    ) {
        StatusIndicator(
            label = stringResource(R.string.dashboard_status_driving),
            color = MaterialTheme.colorScheme.secondary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            // Fixed slots: the gear stays centered and nothing moves sideways when a value changes width.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(64.dp),
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    ValueWithUnit(
                        value = state.speedKmh.valueOrNull()?.toString(),
                        unit = stringResource(R.string.dashboard_unit_kmh),
                        valueStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 280.sp, lineHeight = 288.sp),
                        unitStyle = MaterialTheme.typography.headlineLarge,
                    )
                }
                GearIndicator(
                    gear = state.gear.valueOrNull()?.shortLabel(),
                    filled = state.gear.valueOrNull() == Gear.Park,
                    size = 176.dp,
                    textStyle = MaterialTheme.typography.displayMedium,
                )
                RangeCard(
                    rangeKm = state.rangeKm,
                    title = stringResource(R.string.dashboard_estimated_range),
                    valueStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 144.sp, lineHeight = 152.sp),
                    fillsCell = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BatteryCard(state: DashboardState, modifier: Modifier = Modifier) {
    MetricCard(title = stringResource(R.string.dashboard_battery), modifier = modifier) {
        Spacer(Modifier.weight(1f))
        val percent = state.batteryPercent.valueOrNull()
        ValueWithUnit(
            value = percent?.toString(),
            unit = stringResource(R.string.dashboard_unit_percent),
            valueStyle = MaterialTheme.typography.displaySmall,
            unitStyle = MaterialTheme.typography.headlineSmall,
            valueColor = MaterialTheme.colorScheme.primary,
        )
        if (percent != null) {
            Spacer(Modifier.height(24.dp))
            BatteryBar(percent = percent, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.weight(1f))
        val energy = state.batteryEnergyKwh.valueOrNull()
        val capacity = state.batteryCapacityKwh.valueOrNull()
        val energyText = when {
            energy != null && capacity != null -> stringResource(R.string.dashboard_battery_energy, energy, capacity)
            energy != null -> stringResource(R.string.dashboard_battery_energy_only, energy)
            else -> null
        }
        if (energyText != null) {
            Text(
                text = energyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** In the parked grid the card fills its cell and centers the value; while driving it wraps its content. */
@Composable
private fun RangeCard(
    rangeKm: Reading<Int>,
    title: String,
    valueStyle: TextStyle,
    modifier: Modifier = Modifier,
    fillsCell: Boolean = true,
) {
    MetricCard(title = title, modifier = modifier) {
        if (fillsCell) Spacer(Modifier.weight(1f)) else Spacer(Modifier.height(16.dp))
        ValueWithUnit(
            value = rangeKm.valueOrNull()?.toString(),
            unit = stringResource(R.string.dashboard_unit_km),
            valueStyle = valueStyle,
            unitStyle = MaterialTheme.typography.headlineSmall,
        )
        if (fillsCell) Spacer(Modifier.weight(1f))
    }
}

private val ScreenPadding = 32.dp
private val CardSpacing = 32.dp

@Preview(name = "Parked", widthDp = 1920, heightDp = 1080)
@Composable
private fun DashboardParkedPreview() {
    TruckCockpitTheme { DashboardScreen(DashboardSamples.parked, onIntent = {}) }
}

@Preview(name = "Driving", widthDp = 1920, heightDp = 1080)
@Composable
private fun DashboardDrivingPreview() {
    TruckCockpitTheme { DashboardScreen(DashboardSamples.driving, onIntent = {}) }
}

@Preview(name = "Not available (parked)", widthDp = 1920, heightDp = 1080)
@Composable
private fun DashboardNotAvailablePreview() {
    TruckCockpitTheme { DashboardScreen(DashboardSamples.notAvailableParked, onIntent = {}) }
}
