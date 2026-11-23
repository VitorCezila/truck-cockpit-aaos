package com.cezila.truckcockpit.feature.dashboard

import app.cash.turbine.test
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.testing.MainDispatcherRule
import com.cezila.truckcockpit.core.domain.vehicle.FakeVehicleDataSource
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val vehicle = VehicleState(
        speedKmh = Reading.Available(79.92f),
        gear = Reading.Available(Gear.Drive),
        batteryEnergyKwh = Reading.Available(412.4f),
        batteryCapacityKwh = Reading.Available(540f),
        rangeKm = Reading.Available(296.6f),
    )

    @Test
    fun `before any data the state is the safe default`() {
        val viewModel = DashboardViewModel(FakeVehicleDataSource())

        val state = viewModel.state.value

        assertEquals(DashboardState(), state)
        assertEquals(DrivingMode.Driving, state.drivingMode)
    }

    @Test
    fun `vehicle values are rounded for display`() = runTest {
        val source = FakeVehicleDataSource(initialState = vehicle, initialDrivingMode = DrivingMode.Parked)
        val viewModel = DashboardViewModel(source)

        viewModel.state.test {
            assertEquals(
                DashboardState(
                    drivingMode = DrivingMode.Parked,
                    speedKmh = Reading.Available(80),
                    gear = Reading.Available(Gear.Drive),
                    batteryPercent = Reading.Available(76),
                    batteryEnergyKwh = Reading.Available(412),
                    batteryCapacityKwh = Reading.Available(540),
                    rangeKm = Reading.Available(297),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `a vehicle update reaches the state`() = runTest {
        val source = FakeVehicleDataSource(initialState = vehicle, initialDrivingMode = DrivingMode.Parked)
        val viewModel = DashboardViewModel(source)

        viewModel.state.test {
            assertEquals(Reading.Available(80), awaitItem().speedKmh)

            source.updateVehicleState { it.copy(speedKmh = Reading.Available(50f)) }

            assertEquals(Reading.Available(50), awaitItem().speedKmh)
        }
    }

    @Test
    fun `layout follows UX restrictions, parked to driving and back`() = runTest {
        val source = FakeVehicleDataSource(initialState = vehicle, initialDrivingMode = DrivingMode.Parked)
        val viewModel = DashboardViewModel(source)

        viewModel.state.test {
            assertEquals(DrivingMode.Parked, awaitItem().drivingMode)

            source.setDrivingMode(DrivingMode.Driving)
            val driving = awaitItem()
            assertEquals(DrivingMode.Driving, driving.drivingMode)
            // Vehicle values are kept across the switch.
            assertEquals(Reading.Available(80), driving.speedKmh)

            source.setDrivingMode(DrivingMode.Parked)
            assertEquals(DrivingMode.Parked, awaitItem().drivingMode)
        }
    }

    @Test
    fun `unavailable properties stay not available, the rest still shows`() = runTest {
        val source = FakeVehicleDataSource(
            initialState = vehicle.copy(
                speedKmh = Reading.NotAvailable,
                batteryCapacityKwh = Reading.NotAvailable,
            ),
            initialDrivingMode = DrivingMode.Parked,
        )
        val viewModel = DashboardViewModel(source)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(Reading.NotAvailable, state.speedKmh)
            assertEquals(Reading.NotAvailable, state.batteryCapacityKwh)
            assertEquals(Reading.NotAvailable, state.batteryPercent)
            assertEquals(Reading.Available(412), state.batteryEnergyKwh)
            assertEquals(Reading.Available(Gear.Drive), state.gear)
            assertEquals(Reading.Available(297), state.rangeKm)
        }
    }

    @Test
    fun `nothing available maps to an all not available state`() = runTest {
        val viewModel = DashboardViewModel(FakeVehicleDataSource(initialDrivingMode = DrivingMode.Parked))

        viewModel.state.test {
            assertEquals(DashboardState(drivingMode = DrivingMode.Parked), awaitItem())
        }
    }

    @Test
    fun `after the screen stops for longer than the timeout, the state goes back to the safe default`() = runTest {
        val source = FakeVehicleDataSource(initialState = vehicle, initialDrivingMode = DrivingMode.Parked)
        val viewModel = DashboardViewModel(source)

        viewModel.state.test {
            assertEquals(DrivingMode.Parked, awaitItem().drivingMode)
        }
        advanceTimeBy(DashboardViewModel.STOP_TIMEOUT_MILLIS + 1)
        runCurrent()

        assertEquals(DashboardState(), viewModel.state.value)
        assertEquals(0, source.vehicleStateSubscribers.value)
    }

    @Test
    fun `a short stop, like a rotation, keeps the state`() = runTest {
        val source = FakeVehicleDataSource(initialState = vehicle, initialDrivingMode = DrivingMode.Parked)
        val viewModel = DashboardViewModel(source)

        viewModel.state.test {
            assertEquals(DrivingMode.Parked, awaitItem().drivingMode)
        }
        advanceTimeBy(DashboardViewModel.STOP_TIMEOUT_MILLIS - 1)
        runCurrent()

        assertEquals(DrivingMode.Parked, viewModel.state.value.drivingMode)
        assertEquals(Reading.Available(80), viewModel.state.value.speedKmh)
    }
}
