package com.cezila.truckcockpit.core.data.trip

import com.cezila.truckcockpit.core.data.di.ApplicationScope
import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.TripProgress
import com.cezila.truckcockpit.core.domain.trip.TripRepository
import com.cezila.truckcockpit.core.domain.usecase.UpdateTripProgressUseCase
import com.cezila.truckcockpit.core.domain.vehicle.VehicleDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Tracks the trip in the application scope, so it keeps counting while the Trip screen is not
 * shown. Samples the latest vehicle state every [SAMPLE_INTERVAL]: sampling on a clock, rather
 * than on each vehicle update, keeps adding distance at a steady speed, when no value changes.
 *
 * Tracking stops once the destination is reached. The trip lives in memory only and is lost when
 * the process dies.
 */
@Singleton
internal class TrackingTripRepository @Inject constructor(
    private val vehicleDataSource: VehicleDataSource,
    private val updateTripProgress: UpdateTripProgressUseCase,
    @ApplicationScope private val scope: CoroutineScope,
) : TripRepository {

    override val destinations: List<Destination> = SimulatedDestinations.all

    private val _trip = MutableStateFlow<TripProgress?>(null)
    override val trip: StateFlow<TripProgress?> = _trip.asStateFlow()

    /** Only touched from [startTrip], which the UI calls on the main thread. */
    private var tracking: Job? = null

    override fun startTrip(destination: Destination) {
        val previous = tracking
        tracking = scope.launch {
            // Wait for the old trip to stop, so none of its samples lands on the new one.
            previous?.cancelAndJoin()
            _trip.value = TripProgress(destination)
            val vehicle = vehicleDataSource.vehicleState.stateIn(this)
            // Stops at the destination, which also releases the vehicle data subscription.
            while (_trip.value?.remainingKm != 0f) {
                delay(SAMPLE_INTERVAL)
                _trip.update { progress -> progress?.let { updateTripProgress(it, vehicle.value, SAMPLE_INTERVAL) } }
            }
            // stateIn collects in a child coroutine that never completes by itself: stop it explicitly.
            coroutineContext.cancelChildren()
        }
    }

    companion object {
        val SAMPLE_INTERVAL: Duration = 1.seconds
    }
}
