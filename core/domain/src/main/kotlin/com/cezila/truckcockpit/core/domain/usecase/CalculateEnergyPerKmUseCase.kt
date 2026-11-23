package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripProgress
import com.cezila.truckcockpit.core.domain.model.valueOrNull
import javax.inject.Inject

/**
 * Average energy use of the trip, in kWh/km: battery energy used since the baseline over the
 * distance covered since then.
 *
 * Not available until [TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM] have been covered since the
 * baseline, or without a battery level. Energy gained (regenerative braking downhill) counts against
 * the energy used, but the result never goes below 0.
 */
class CalculateEnergyPerKmUseCase @Inject constructor() {

    operator fun invoke(progress: TripProgress, batteryEnergyKwh: Reading<Float>): Reading<Float> {
        val baseline = progress.energyBaseline ?: return Reading.NotAvailable
        val battery = batteryEnergyKwh.valueOrNull() ?: return Reading.NotAvailable
        val distance = progress.distanceTravelledKm - baseline.distanceKm
        if (distance < TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM) return Reading.NotAvailable
        return Reading.Available(((baseline.batteryKwh - battery) / distance).coerceAtLeast(0f))
    }
}
