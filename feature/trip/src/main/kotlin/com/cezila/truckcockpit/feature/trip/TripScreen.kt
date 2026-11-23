package com.cezila.truckcockpit.feature.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.valueOrNull
import com.cezila.truckcockpit.core.ui.component.AlertBanner
import com.cezila.truckcockpit.core.ui.component.AlertSeverity
import com.cezila.truckcockpit.core.ui.component.BatteryBar
import com.cezila.truckcockpit.core.ui.component.BigButton
import com.cezila.truckcockpit.core.ui.component.BigOutlinedButton
import com.cezila.truckcockpit.core.ui.component.GearIndicator
import com.cezila.truckcockpit.core.ui.component.MetricCard
import com.cezila.truckcockpit.core.ui.component.MinTouchTarget
import com.cezila.truckcockpit.core.ui.component.StatusIndicator
import com.cezila.truckcockpit.core.ui.component.ValueWithUnit
import com.cezila.truckcockpit.core.ui.component.shortLabel
import com.cezila.truckcockpit.core.ui.theme.TruckCockpitTheme
import com.cezila.truckcockpit.core.ui.R as CoreUiR
import kotlin.math.roundToInt

/** The only composable that sees the ViewModel. */
@Composable
fun TripRoute(
    modifier: Modifier = Modifier,
    viewModel: TripViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // No effects yet.
            viewModel.effects.collect { }
        }
    }
    TripScreen(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

/** Stateless: full layout when parked, the essentials and the warning while driving. */
@Composable
fun TripScreen(
    state: TripState,
    onIntent: (TripIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(ScreenPadding),
        ) {
            when (state.drivingMode) {
                DrivingMode.Parked -> {
                    StatusIndicator(
                        label = stringResource(R.string.trip_status_parked),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(24.dp))
                    if (state.showsDestinationPicker) {
                        DestinationPicker(state, onIntent, Modifier.weight(1f))
                    } else {
                        TripParkedContent(state, onIntent, Modifier.weight(1f))
                    }
                }
                DrivingMode.Driving -> TripDrivingContent(state)
            }
        }
    }
}

/** Destination and time on the left, battery and energy use on the right, status at the bottom. */
@Composable
private fun TripParkedContent(state: TripState, onIntent: (TripIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(CardSpacing)) {
        MetricCard(
            title = stringResource(R.string.trip_destination),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
            Text(
                text = state.destination?.name ?: stringResource(R.string.trip_no_destination),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(CardSpacing)) {
                LabeledValue(stringResource(R.string.trip_remaining_distance), Modifier.weight(1f)) {
                    ValueWithUnit(
                        value = state.remainingKm.valueOrNull()?.toString(),
                        unit = stringResource(R.string.trip_unit_km),
                        valueStyle = MaterialTheme.typography.displaySmall,
                        unitStyle = MaterialTheme.typography.titleLarge,
                    )
                }
                LabeledValue(stringResource(R.string.trip_time_to_arrival), Modifier.weight(1f)) {
                    TimeToArrival(state.timeToArrivalMinutes)
                }
            }
            Spacer(Modifier.weight(1f))
            BigButton(
                text = stringResource(R.string.trip_change_destination),
                onClick = { onIntent(TripIntent.ChangeDestinationClicked) },
            )
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(CardSpacing),
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CardSpacing),
            ) {
                BatteryAtArrivalCard(state, Modifier.weight(1f).fillMaxHeight())
                MetricCard(
                    title = stringResource(R.string.trip_average_use),
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                ) {
                    Spacer(Modifier.weight(1f))
                    EnergyPerKm(state.energyPerKm)
                    Spacer(Modifier.weight(1f))
                }
            }
            DestinationStatusBanner(state, Modifier.fillMaxWidth())
        }
    }
}

/** The unit goes under the number: "kWh/km" does not fit beside it in a half-width card at 1408dp. */
@Composable
private fun EnergyPerKm(energyPerKm: Reading<Float>) {
    val value = energyPerKm.valueOrNull()?.let { stringResource(R.string.trip_energy_value, it) }
    val unit = stringResource(R.string.trip_unit_kwh_per_km)
    if (value == null) {
        ValueWithUnit(
            value = null,
            unit = unit,
            valueStyle = MaterialTheme.typography.displaySmall,
            unitStyle = MaterialTheme.typography.titleLarge,
        )
        return
    }
    Column(modifier = Modifier.clearAndSetSemantics { contentDescription = "$value $unit" }) {
        Text(text = value, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
        Text(text = unit, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BatteryAtArrivalCard(state: TripState, modifier: Modifier = Modifier) {
    MetricCard(title = stringResource(R.string.trip_battery_at_arrival), modifier = modifier) {
        Spacer(Modifier.weight(1f))
        val percent = state.batteryAtArrivalPercent.valueOrNull()
        if (state.destinationStatus is DestinationStatus.Unreachable) {
            Text(
                text = stringResource(R.string.trip_below_reserve),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            ValueWithUnit(
                value = percent?.toString(),
                unit = stringResource(R.string.trip_unit_percent),
                valueStyle = MaterialTheme.typography.displaySmall,
                unitStyle = MaterialTheme.typography.titleLarge,
                valueColor = MaterialTheme.colorScheme.primary,
            )
            if (percent != null) {
                Spacer(Modifier.height(24.dp))
                BatteryBar(percent = percent, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DestinationStatusBanner(state: TripState, modifier: Modifier = Modifier) {
    val status = state.destinationStatus
    when {
        state.destination == null -> Unit
        status is DestinationStatus.Unreachable -> AlertBanner(
            title = stringResource(R.string.trip_unreachable),
            message = stringResource(R.string.trip_unreachable_message, status.shortByKm),
            severity = AlertSeverity.Critical,
            modifier = modifier,
        )
        status == DestinationStatus.Reachable -> AlertBanner(
            title = stringResource(R.string.trip_reachable),
            message = state.batteryAtArrivalPercent.valueOrNull()
                ?.let { stringResource(R.string.trip_reachable_message, it) },
            severity = AlertSeverity.Ok,
            modifier = modifier,
        )
        else -> AlertBanner(
            title = stringResource(R.string.trip_estimating),
            message = stringResource(R.string.trip_estimating_message),
            severity = AlertSeverity.Warning,
            modifier = modifier,
        )
    }
}

/** Parked only: the destinations as large buttons in a fixed grid, no scrolling, no text input. */
@Composable
private fun DestinationPicker(state: TripState, onIntent: (TripIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CardSpacing)) {
        Text(
            text = stringResource(R.string.trip_choose_destination),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        state.destinations.chunked(PickerColumns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(CardSpacing)) {
                row.forEach { destination ->
                    DestinationButton(
                        destination = destination,
                        selected = destination == state.destination,
                        onClick = { onIntent(TripIntent.DestinationSelected(destination)) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keeps a lone last button at the same width as the others.
                repeat(PickerColumns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        if (state.destination != null) {
            BigOutlinedButton(
                text = stringResource(R.string.trip_cancel),
                onClick = { onIntent(TripIntent.DestinationPickerDismissed) },
                modifier = Modifier.width(320.dp),
            )
        }
    }
}

@Composable
private fun DestinationButton(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinTouchTarget + 32.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 4.dp else 2.dp,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = destination.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.trip_destination_distance, destination.distanceKm.roundToInt()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Driving: speed, gear and range on top; in the middle only the warning, or distance and arrival battery. */
@Composable
private fun TripDrivingContent(state: TripState) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        StatusIndicator(
            label = stringResource(R.string.trip_status_driving),
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.weight(1f))
        // No labels in this row: a missing value keeps its unit ("— km") so it is clear which one it is.
        ValueWithUnit(
            value = state.speedKmh.valueOrNull()?.toString() ?: stringResource(CoreUiR.string.core_ui_value_placeholder),
            unit = stringResource(R.string.trip_unit_kmh),
            valueStyle = MaterialTheme.typography.displaySmall,
            unitStyle = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.width(40.dp))
        GearIndicator(
            gear = state.gear.valueOrNull()?.shortLabel(),
            filled = state.gear.valueOrNull() == Gear.Park,
            size = 96.dp,
            textStyle = MaterialTheme.typography.headlineLarge,
        )
        Spacer(Modifier.width(40.dp))
        ValueWithUnit(
            value = state.rangeKm.valueOrNull()?.toString() ?: stringResource(CoreUiR.string.core_ui_value_placeholder),
            unit = stringResource(R.string.trip_unit_km),
            valueStyle = MaterialTheme.typography.displaySmall,
            unitStyle = MaterialTheme.typography.titleLarge,
        )
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val status = state.destinationStatus
        when {
            state.destination == null -> AlertBanner(
                title = stringResource(R.string.trip_no_destination),
                message = stringResource(R.string.trip_no_destination_driving),
                severity = AlertSeverity.Warning,
                modifier = Modifier.fillMaxWidth(),
            )
            status is DestinationStatus.Unreachable -> AlertBanner(
                title = stringResource(R.string.trip_unreachable),
                message = stringResource(R.string.trip_unreachable_message, status.shortByKm),
                severity = AlertSeverity.Critical,
                titleStyle = MaterialTheme.typography.displaySmall,
                messageStyle = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            else -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CardSpacing),
            ) {
                MetricCard(title = stringResource(R.string.trip_remaining_distance), modifier = Modifier.weight(1f)) {
                    Spacer(Modifier.height(16.dp))
                    ValueWithUnit(
                        value = state.remainingKm.valueOrNull()?.toString(),
                        unit = stringResource(R.string.trip_unit_km),
                        valueStyle = drivingValueStyle(),
                        unitStyle = MaterialTheme.typography.headlineSmall,
                    )
                }
                MetricCard(title = stringResource(R.string.trip_battery_at_arrival), modifier = Modifier.weight(1f)) {
                    Spacer(Modifier.height(16.dp))
                    val percent = state.batteryAtArrivalPercent.valueOrNull()
                    if (percent == null && status == DestinationStatus.Unknown) {
                        // Says why there is no value yet, instead of a bare "Not available".
                        Text(
                            text = stringResource(R.string.trip_estimating),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    } else {
                        ValueWithUnit(
                            value = percent?.toString(),
                            unit = stringResource(R.string.trip_unit_percent),
                            valueStyle = drivingValueStyle(),
                            unitStyle = MaterialTheme.typography.headlineSmall,
                            valueColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun drivingValueStyle() = MaterialTheme.typography.displayLarge.copy(fontSize = 136.sp, lineHeight = 144.sp)

@Composable
private fun LabeledValue(label: String, modifier: Modifier = Modifier, value: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        value()
    }
}

/** "45 min" under an hour, "2:35 h" from an hour up. */
@Composable
private fun TimeToArrival(minutes: Reading<Int>) {
    val total = minutes.valueOrNull()
    ValueWithUnit(
        value = total?.let {
            if (it < 60) it.toString() else stringResource(R.string.trip_duration_hours_minutes, it / 60, it % 60)
        },
        unit = stringResource(if ((total ?: 0) < 60) R.string.trip_unit_minutes else R.string.trip_unit_hours),
        valueStyle = MaterialTheme.typography.displaySmall,
        unitStyle = MaterialTheme.typography.titleLarge,
    )
}

private val ScreenPadding = 32.dp
private val CardSpacing = 32.dp
private const val PickerColumns = 2

@Preview(name = "Parked", widthDp = 1920, heightDp = 1080)
@Composable
private fun TripParkedPreview() {
    TruckCockpitTheme { TripScreen(TripSamples.parked, onIntent = {}) }
}

@Preview(name = "Driving, unreachable", widthDp = 1920, heightDp = 1080)
@Composable
private fun TripDrivingUnreachablePreview() {
    TruckCockpitTheme { TripScreen(TripSamples.unreachableDriving, onIntent = {}) }
}

@Preview(name = "Driving", widthDp = 1920, heightDp = 1080)
@Composable
private fun TripDrivingPreview() {
    TruckCockpitTheme { TripScreen(TripSamples.driving, onIntent = {}) }
}

@Preview(name = "Destination picker", widthDp = 1920, heightDp = 1080)
@Composable
private fun TripPickerPreview() {
    TruckCockpitTheme { TripScreen(TripSamples.pickingDestination, onIntent = {}) }
}

@Preview(name = "Not available (parked)", widthDp = 1920, heightDp = 1080)
@Composable
private fun TripNotAvailablePreview() {
    TruckCockpitTheme { TripScreen(TripSamples.notAvailableParked, onIntent = {}) }
}
