package com.cezila.truckcockpit.feature.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Reachability
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripProgress
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.model.map
import com.cezila.truckcockpit.core.domain.model.toReading
import com.cezila.truckcockpit.core.domain.model.valueOrNull
import com.cezila.truckcockpit.core.domain.trip.TripRepository
import com.cezila.truckcockpit.core.domain.usecase.CalculateEnergyPerKmUseCase
import com.cezila.truckcockpit.core.domain.usecase.CheckDestinationReachableUseCase
import com.cezila.truckcockpit.core.domain.usecase.EstimateTimeToArrivalUseCase
import com.cezila.truckcockpit.core.domain.vehicle.VehicleDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.roundToInt

@HiltViewModel
class TripViewModel @Inject constructor(
    vehicleDataSource: VehicleDataSource,
    private val tripRepository: TripRepository,
    private val calculateEnergyPerKm: CalculateEnergyPerKmUseCase,
    private val checkDestinationReachable: CheckDestinationReachableUseCase,
    private val estimateTimeToArrival: EstimateTimeToArrivalUseCase,
) : ViewModel() {

    /**
     * State owned by this screen: the driving mode it last saw and whether the picker is open.
     * Intents and driving mode changes both go through [TripUiState] reducers, so the parked-only
     * rule and the picker never disagree. Vehicle and trip values come from the data flows.
     */
    private val uiState = MutableStateFlow(TripUiState())

    /**
     * When the screen stops observing, the last mode seen is no longer known: go back to driving, so
     * destination changes stay blocked until a fresh parked value arrives.
     */
    private val drivingMode = vehicleDataSource.drivingMode
        .onEach { mode -> uiState.update { it.withDrivingMode(mode) } }
        .onCompletion { uiState.update { it.withDrivingMode(DrivingMode.Driving) } }

    val state: StateFlow<TripState> = combine(
        vehicleDataSource.vehicleState,
        drivingMode,
        tripRepository.trip,
        uiState,
    ) { vehicle, _, trip, ui -> // The mode is read from ui: drivingMode only feeds uiState.
        TripState(
            drivingMode = ui.drivingMode,
            speedKmh = vehicle.speedKmh.map { it.roundToInt() },
            gear = vehicle.gear,
            rangeKm = vehicle.rangeKm.map { it.roundToInt() },
            destinations = tripRepository.destinations,
            isPickingDestination = ui.isPickingDestination,
        ).withTrip(trip, vehicle)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS, replayExpirationMillis = 0),
        initialValue = TripState(),
    )

    private val _effects = Channel<TripEffect>(Channel.BUFFERED)
    val effects: Flow<TripEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: TripIntent) {
        when (intent) {
            TripIntent.ChangeDestinationClicked -> uiState.update { it.withPickerOpen(true) }
            is TripIntent.DestinationSelected -> if (uiState.value.isParked) {
                // Confirming the current destination keeps the trip's distance and energy use.
                if (intent.destination != state.value.destination) tripRepository.startTrip(intent.destination)
                uiState.update { it.withPickerOpen(false) }
            }
            TripIntent.DestinationPickerDismissed -> uiState.update { it.withPickerOpen(false) }
        }
    }

    private fun TripState.withTrip(trip: TripProgress?, vehicle: VehicleState): TripState {
        if (trip == null) return this
        val energyPerKm = calculateEnergyPerKm(trip, vehicle.batteryEnergyKwh)
        val reachability = checkDestinationReachable(trip.remainingKm, vehicle, energyPerKm.toReading())
        return copy(
            destination = trip.destination,
            remainingKm = Reading.Available(trip.remainingKm.roundToInt()),
            timeToArrivalMinutes = estimateTimeToArrival(trip).map { ceil(it.inWholeSeconds / 60.0).toInt() },
            batteryAtArrivalPercent = reachability.arrivalPercent(vehicle),
            energyPerKm = energyPerKm.map { (it * 100f).roundToInt() / 100f },
            destinationStatus = when (reachability) {
                Reachability.Unknown -> DestinationStatus.Unknown
                is Reachability.Reachable -> DestinationStatus.Reachable
                // Rounded up: the warning never understates the shortfall.
                is Reachability.Unreachable -> DestinationStatus.Unreachable(ceil(reachability.shortByKm).toInt())
            },
        )
    }

    private fun Reachability.arrivalPercent(vehicle: VehicleState): Reading<Int> {
        val arrivalKwh = (this as? Reachability.Reachable)?.batteryAtArrivalKwh ?: return Reading.NotAvailable
        val capacity = vehicle.batteryCapacityKwh.valueOrNull()?.takeIf { it > 0f } ?: return Reading.NotAvailable
        return Reading.Available((arrivalKwh / capacity * 100f).roundToInt().coerceIn(0, 100))
    }

    /** Starts as driving, the safe assumption: destination changes stay blocked until parked is confirmed. */
    private data class TripUiState(
        val drivingMode: DrivingMode = DrivingMode.Driving,
        val isPickingDestination: Boolean = false,
    ) {
        val isParked: Boolean get() = drivingMode == DrivingMode.Parked

        /** Destination changes are a parked-only task: the picker only opens when parked. */
        fun withPickerOpen(open: Boolean): TripUiState = copy(isPickingDestination = open && isParked)

        /** Moving closes the picker, so it does not reopen by itself once parked again. */
        fun withDrivingMode(mode: DrivingMode): TripUiState = copy(
            drivingMode = mode,
            isPickingDestination = isPickingDestination && mode == DrivingMode.Parked,
        )
    }

    internal companion object {
        /**
         * Keeps the Car API subscriptions across configuration changes. Once it runs out, the state
         * goes back to the initial (driving) one, so a screen coming back never shows an old parked layout.
         */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
