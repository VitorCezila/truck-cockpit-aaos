package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripAverage
import com.cezila.truckcockpit.core.domain.model.TripProgress
import com.cezila.truckcockpit.core.domain.model.valueOrNull
import javax.inject.Inject

/**
 * Average energy use of the trip, in kWh/km: battery energy used since the baseline over the
 * distance covered since then. Energy gained (regenerative braking downhill) counts against the
 * energy used, but the result never goes below 0.
 *
 * Not available without a battery level. Pending until [TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM]
 * have been covered since the baseline; with the battery readable but no baseline yet, the trip
 * takes one on its next sample ([UpdateTripProgressUseCase]), so that is pending too.
 */
class CalculateEnergyPerKmUseCase @Inject constructor() {

    operator fun invoke(progress: TripProgress, batteryEnergyKwh: Reading<Float>): TripAverage<Float> {
        val battery = batteryEnergyKwh.valueOrNull() ?: return TripAverage.NotAvailable
        val baseline = progress.energyBaseline
            ?: return TripAverage.Pending(TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM)
        val distance = progress.distanceTravelledKm - baseline.distanceKm
        if (distance < TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM) {
            return TripAverage.Pending(TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM - distance)
        }
        return TripAverage.Available(((baseline.batteryKwh - battery) / distance).coerceAtLeast(0f))
    }
}
