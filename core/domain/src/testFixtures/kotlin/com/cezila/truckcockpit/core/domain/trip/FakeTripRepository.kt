package com.cezila.truckcockpit.core.domain.trip

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.TripProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** [TripRepository] for tests: no tracking, the trip is whatever you set. */
class FakeTripRepository(
    override val destinations: List<Destination> = emptyList(),
    initialTrip: TripProgress? = null,
) : TripRepository {
    private val _trip = MutableStateFlow(initialTrip)
    override val trip: StateFlow<TripProgress?> = _trip

    override fun startTrip(destination: Destination) {
        _trip.value = TripProgress(destination)
    }

    fun setTrip(trip: TripProgress?) {
        _trip.value = trip
    }
}
