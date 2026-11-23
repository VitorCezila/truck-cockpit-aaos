package com.cezila.truckcockpit.feature.dashboard

import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading

/** States for previews and snapshot tests. */
internal object DashboardSamples {
    val parked = DashboardState(
        drivingMode = DrivingMode.Parked,
        speedKmh = Reading.Available(0),
        gear = Reading.Available(Gear.Park),
        batteryPercent = Reading.Available(76),
        batteryEnergyKwh = Reading.Available(412),
        batteryCapacityKwh = Reading.Available(540),
        rangeKm = Reading.Available(296),
    )

    val driving = parked.copy(
        drivingMode = DrivingMode.Driving,
        speedKmh = Reading.Available(82),
        gear = Reading.Available(Gear.Drive),
    )

    val notAvailableParked = DashboardState(drivingMode = DrivingMode.Parked)

    val notAvailableDriving = DashboardState(drivingMode = DrivingMode.Driving)
}
