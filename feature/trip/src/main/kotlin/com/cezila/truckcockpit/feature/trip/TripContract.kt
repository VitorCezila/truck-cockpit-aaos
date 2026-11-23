package com.cezila.truckcockpit.feature.trip

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading

/** Everything the Trip screen shows, already rounded for display. */
data class TripState(
    /** Until UX restrictions are known, assume driving: the reduced layout is the safe one. */
    val drivingMode: DrivingMode = DrivingMode.Driving,
    val speedKmh: Reading<Int> = Reading.NotAvailable,
    val gear: Reading<Gear> = Reading.NotAvailable,
    val rangeKm: Reading<Int> = Reading.NotAvailable,
    /** Null until the driver picks one. */
    val destination: Destination? = null,
    val remainingKm: Reading<Int> = Reading.NotAvailable,
    val timeToArrivalMinutes: Reading<Int> = Reading.NotAvailable,
    val batteryAtArrivalPercent: Reading<Int> = Reading.NotAvailable,
    /** Trip average, rounded to two decimals. */
    val energyPerKm: Reading<Float> = Reading.NotAvailable,
    val destinationStatus: DestinationStatus = DestinationStatus.Unknown,
    /** Offered in the picker, parked only. */
    val destinations: List<Destination> = emptyList(),
    /** The destination picker is open. Always false while driving. */
    val isPickingDestination: Boolean = false,
) {
    /** The picker replaces the parked content when asked for, and when no destination is set yet. */
    val showsDestinationPicker: Boolean
        get() = drivingMode == DrivingMode.Parked && (isPickingDestination || destination == null)
}

/** Whether the battery gets the truck to the destination. */
sealed interface DestinationStatus {
    /** No destination, or not enough data to tell. */
    data object Unknown : DestinationStatus

    data object Reachable : DestinationStatus

    data class Unreachable(val shortByKm: Int) : DestinationStatus
}

sealed interface TripIntent {
    /** Opens the destination picker. Ignored while driving. */
    data object ChangeDestinationClicked : TripIntent

    /** Starts a new trip. Ignored while driving. */
    data class DestinationSelected(val destination: Destination) : TripIntent

    data object DestinationPickerDismissed : TripIntent
}

/** One-shot events for the UI. None yet. */
sealed interface TripEffect
