package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.EnergyBaseline
import com.cezila.truckcockpit.core.domain.model.TripProgress
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.model.valueOrNull
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Advances a trip by one vehicle sample: adds the distance covered at the sampled speed over
 * [elapsed] (speed held constant between samples), and records the energy baseline the first time
 * the battery level is known.
 *
 * An unknown speed adds no distance: the trip pauses rather than guessing.
 */
class UpdateTripProgressUseCase @Inject constructor() {

    operator fun invoke(progress: TripProgress, vehicle: VehicleState, elapsed: Duration): TripProgress {
        val step = elapsed.coerceIn(Duration.ZERO, MAX_SAMPLE_GAP)
        val hours = step.toDouble(DurationUnit.HOURS).toFloat()
        val speedKmh = vehicle.speedKmh.valueOrNull()?.coerceAtLeast(0f) ?: 0f
        val moving = speedKmh > 0f && hours > 0f
        val distance = progress.distanceTravelledKm + speedKmh * hours
        val baseline = progress.energyBaseline
            ?: vehicle.batteryEnergyKwh.valueOrNull()?.let { EnergyBaseline(batteryKwh = it, distanceKm = distance) }
        return progress.copy(
            distanceTravelledKm = distance,
            movingTime = if (moving) progress.movingTime + step else progress.movingTime,
            energyBaseline = baseline,
        )
    }

    companion object {
        /**
         * A longer gap between samples (the process was paused, the car service reconnected) is
         * capped, so one stale speed value cannot add a large jump in distance.
         */
        val MAX_SAMPLE_GAP: Duration = 5.seconds
    }
}
