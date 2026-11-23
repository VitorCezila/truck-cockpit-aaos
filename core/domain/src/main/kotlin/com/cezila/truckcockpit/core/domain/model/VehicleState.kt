package com.cezila.truckcockpit.core.domain.model

import kotlin.math.roundToInt

/** Snapshot of the vehicle data the app reads, in domain units. */
data class VehicleState(
    val speedKmh: Reading<Float> = Reading.NotAvailable,
    val gear: Reading<Gear> = Reading.NotAvailable,
    val batteryEnergyKwh: Reading<Float> = Reading.NotAvailable,
    val batteryCapacityKwh: Reading<Float> = Reading.NotAvailable,
    val rangeKm: Reading<Float> = Reading.NotAvailable,
) {
    /** Battery level in percent (0..100). Needs both the energy left and the capacity. */
    val batteryPercent: Reading<Int>
        get() {
            val energy = batteryEnergyKwh.valueOrNull() ?: return Reading.NotAvailable
            val capacity = batteryCapacityKwh.valueOrNull()?.takeIf { it > 0f } ?: return Reading.NotAvailable
            return Reading.Available((energy / capacity * 100f).roundToInt().coerceIn(0, 100))
        }

    companion object {
        /** Nothing read yet, or nothing readable. */
        val Unknown = VehicleState()
    }
}
