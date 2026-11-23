package com.cezila.truckcockpit.core.domain.vehicle

import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.VehicleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** [VehicleDataSource] for tests: emits whatever state you set. */
class FakeVehicleDataSource(
    initialState: VehicleState = VehicleState.Unknown,
    initialDrivingMode: DrivingMode = DrivingMode.Parked,
) : VehicleDataSource {
    private val _vehicleState = MutableStateFlow(initialState)
    private val _drivingMode = MutableStateFlow(initialDrivingMode)

    override val vehicleState: StateFlow<VehicleState> = _vehicleState
    override val drivingMode: StateFlow<DrivingMode> = _drivingMode

    /** How many collectors [vehicleState] has: checks that a subscription is released. */
    val vehicleStateSubscribers: StateFlow<Int> get() = _vehicleState.subscriptionCount

    fun setVehicleState(state: VehicleState) {
        _vehicleState.value = state
    }

    fun updateVehicleState(transform: (VehicleState) -> VehicleState) {
        _vehicleState.value = transform(_vehicleState.value)
    }

    fun setDrivingMode(mode: DrivingMode) {
        _drivingMode.value = mode
    }
}
