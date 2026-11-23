package com.cezila.truckcockpit.core.domain.model

import kotlin.time.Duration

/**
 * How far the current trip has gone. Distance comes from integrating speed over time, so it is an
 * estimate.
 */
data class TripProgress(
    val destination: Destination,
    val distanceTravelledKm: Float = 0f,
    /** Time spent moving (speed above 0). Used for the average speed. */
    val movingTime: Duration = Duration.ZERO,
    /** First known battery level of the trip; null until the battery has been read. */
    val energyBaseline: EnergyBaseline? = null,
) {
    val remainingKm: Float
        get() = (destination.distanceKm - distanceTravelledKm).coerceAtLeast(0f)

    companion object {
        /**
         * Distance before trip averages (energy per km, speed) are trusted: over a few hundred
         * meters they are mostly noise from accelerating.
         */
        const val MIN_DISTANCE_FOR_AVERAGES_KM = 1f
    }
}

/**
 * Battery energy at a point of the trip. Energy use is measured from here, so distance covered
 * before the battery was first read does not count.
 */
data class EnergyBaseline(
    val batteryKwh: Float,
    val distanceKm: Float,
)
