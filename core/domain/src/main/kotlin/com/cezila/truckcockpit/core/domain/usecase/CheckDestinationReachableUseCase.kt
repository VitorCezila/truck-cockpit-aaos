package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Reachability
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.model.valueOrNull
import javax.inject.Inject

/**
 * Checks whether the battery gets the truck to its destination with [RESERVE_FRACTION] of the
 * capacity left.
 *
 * Energy use per km is the trip's measured average when there is one, otherwise the vehicle's own
 * estimate (battery energy over remaining range). Without the capacity there is no reserve.
 */
class CheckDestinationReachableUseCase @Inject constructor() {

    operator fun invoke(
        remainingKm: Float,
        vehicle: VehicleState,
        tripEnergyPerKm: Reading<Float>,
    ): Reachability {
        val battery = vehicle.batteryEnergyKwh.valueOrNull() ?: return Reachability.Unknown
        val remaining = remainingKm.coerceAtLeast(0f)
        val reserve = (vehicle.batteryCapacityKwh.valueOrNull() ?: 0f) * RESERVE_FRACTION
        val usable = battery - reserve
        // Already at the reserve: whatever the energy use, nothing of the remaining distance is covered.
        if (usable <= 0f) {
            return if (remaining == 0f) Reachability.Reachable(battery) else Reachability.Unreachable(remaining)
        }
        val kwhPerKm = tripEnergyPerKm.valueOrNull()?.takeIf { it > 0f }
            ?: vehicle.rangeKm.valueOrNull()?.takeIf { it > 0f }?.let { battery / it }
            ?: return Reachability.Unknown

        val needed = remaining * kwhPerKm
        return if (needed <= usable) {
            Reachability.Reachable(batteryAtArrivalKwh = battery - needed)
        } else {
            Reachability.Unreachable(shortByKm = (needed - usable) / kwhPerKm)
        }
    }

    companion object {
        /** Share of the battery capacity kept in reserve at arrival. */
        const val RESERVE_FRACTION = 0.05f
    }
}
