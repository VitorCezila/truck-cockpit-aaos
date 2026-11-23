package com.cezila.truckcockpit.core.domain.vehicle

import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.VehicleState
import kotlinx.coroutines.flow.Flow

/** The only way into vehicle data. Implementations hide the Car API. */
interface VehicleDataSource {
    /** Latest vehicle values. Fast-changing values (speed) are already throttled. */
    val vehicleState: Flow<VehicleState>

    /** Parked or driving, from the vehicle's UX restrictions. */
    val drivingMode: Flow<DrivingMode>
}
