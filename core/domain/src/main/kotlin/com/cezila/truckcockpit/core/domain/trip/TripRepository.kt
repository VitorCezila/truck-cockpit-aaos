package com.cezila.truckcockpit.core.domain.trip

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.TripProgress
import kotlinx.coroutines.flow.Flow

/**
 * The current trip. It keeps tracking while the screens are not visible, so switching tabs does
 * not lose distance or energy use.
 */
interface TripRepository {
    /** Destinations the driver can pick. Simulated: there is no navigation on the head unit. */
    val destinations: List<Destination>

    /** The trip in progress, or null when no destination is set. */
    val trip: Flow<TripProgress?>

    /** Starts a new trip to [destination], dropping the current one. */
    fun startTrip(destination: Destination)
}
