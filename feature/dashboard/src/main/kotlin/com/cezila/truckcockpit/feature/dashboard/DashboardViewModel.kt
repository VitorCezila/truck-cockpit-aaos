package com.cezila.truckcockpit.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cezila.truckcockpit.core.domain.vehicle.VehicleDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    vehicleDataSource: VehicleDataSource,
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        vehicleDataSource.vehicleState,
        vehicleDataSource.drivingMode,
    ) { vehicle, drivingMode ->
        DashboardState(drivingMode = drivingMode).withVehicle(vehicle)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS, replayExpirationMillis = 0),
        initialValue = DashboardState(),
    )

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects: Flow<DashboardEffect> = _effects.receiveAsFlow()

    @Suppress("UNUSED_PARAMETER") // No intents yet; this stays the single entry point.
    fun onIntent(intent: DashboardIntent) = Unit

    internal companion object {
        /**
         * Keeps the Car API subscriptions across configuration changes. Once it runs out, the state
         * goes back to the initial (driving) one, so a screen coming back never shows an old parked layout.
         */
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
