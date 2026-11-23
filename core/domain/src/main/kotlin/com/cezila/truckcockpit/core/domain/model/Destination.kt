package com.cezila.truckcockpit.core.domain.model

/**
 * Where the trip ends. There is no navigation on the head unit, so a destination is just a name and
 * the road distance to it from the start of the trip.
 */
data class Destination(
    val id: String,
    val name: String,
    val distanceKm: Float,
) {
    init {
        require(distanceKm > 0f) { "Destination distance must be positive, was $distanceKm" }
    }
}
