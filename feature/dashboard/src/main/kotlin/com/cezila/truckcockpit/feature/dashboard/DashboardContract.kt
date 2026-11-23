package com.cezila.truckcockpit.feature.dashboard

import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.model.map
import kotlin.math.roundToInt

/** Everything the Dashboard shows, already rounded for display. */
data class DashboardState(
    /** Until UX restrictions are known, assume driving: the reduced layout is the safe one. */
    val drivingMode: DrivingMode = DrivingMode.Driving,
    val speedKmh: Reading<Int> = Reading.NotAvailable,
    val gear: Reading<Gear> = Reading.NotAvailable,
    val batteryPercent: Reading<Int> = Reading.NotAvailable,
    val batteryEnergyKwh: Reading<Int> = Reading.NotAvailable,
    val batteryCapacityKwh: Reading<Int> = Reading.NotAvailable,
    val rangeKm: Reading<Int> = Reading.NotAvailable,
) {
    /** Returns a copy with the latest vehicle values. */
    fun withVehicle(vehicle: VehicleState): DashboardState = copy(
        speedKmh = vehicle.speedKmh.map { it.roundToInt() },
        gear = vehicle.gear,
        batteryPercent = vehicle.batteryPercent,
        batteryEnergyKwh = vehicle.batteryEnergyKwh.map { it.roundToInt() },
        batteryCapacityKwh = vehicle.batteryCapacityKwh.map { it.roundToInt() },
        rangeKm = vehicle.rangeKm.map { it.roundToInt() },
    )
}

/** User actions. The Dashboard is read-only for now; navigation to Trip arrives with the Trip screen. */
sealed interface DashboardIntent

/** One-shot events for the UI (navigation, messages). None yet. */
sealed interface DashboardEffect
