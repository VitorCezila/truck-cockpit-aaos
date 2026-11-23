package com.cezila.truckcockpit.feature.trip

import app.cash.turbine.test
import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.EnergyBaseline
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripProgress
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.testing.MainDispatcherRule
import com.cezila.truckcockpit.core.domain.trip.FakeTripRepository
import com.cezila.truckcockpit.core.domain.usecase.CalculateEnergyPerKmUseCase
import com.cezila.truckcockpit.core.domain.usecase.CheckDestinationReachableUseCase
import com.cezila.truckcockpit.core.domain.usecase.EstimateTimeToArrivalUseCase
import com.cezila.truckcockpit.core.domain.vehicle.FakeVehicleDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class TripViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val campinas = Destination("campinas", "Campinas Distribution Center", 96f)
    private val curitiba = Destination("curitiba", "Curitiba Logistics Park", 408f)

    /** 540 kWh pack, 412 kWh left; the vehicle estimates 296 km of range. */
    private val vehicle = VehicleState(
        speedKmh = Reading.Available(71.6f),
        gear = Reading.Available(Gear.Drive),
        batteryEnergyKwh = Reading.Available(352f),
        batteryCapacityKwh = Reading.Available(540f),
        rangeKm = Reading.Available(296.4f),
    )

    /** 50 km driven in 40 min using 60 kWh: 1.2 kWh/km at 75 km/h. */
    private val tripToCampinas = TripProgress(
        destination = campinas,
        distanceTravelledKm = 50f,
        movingTime = 40.minutes,
        energyBaseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f),
    )

    private val vehicleSource = FakeVehicleDataSource(initialState = vehicle, initialDrivingMode = DrivingMode.Parked)
    private val trips = FakeTripRepository(destinations = listOf(campinas, curitiba))

    private fun viewModel() = TripViewModel(
        vehicleDataSource = vehicleSource,
        tripRepository = trips,
        calculateEnergyPerKm = CalculateEnergyPerKmUseCase(),
        checkDestinationReachable = CheckDestinationReachableUseCase(),
        estimateTimeToArrival = EstimateTimeToArrivalUseCase(),
    )

    @Test
    fun `before any data the state is the safe default`() {
        val state = viewModel().state.value

        assertEquals(TripState(), state)
        assertEquals(DrivingMode.Driving, state.drivingMode)
    }

    @Test
    fun `without a trip, parked shows the destination picker`() = runTest {
        viewModel().state.test {
            val state = awaitItem()
            assertNull(state.destination)
            assertEquals(listOf(campinas, curitiba), state.destinations)
            assertTrue(state.showsDestinationPicker)
            assertEquals(DestinationStatus.Unknown, state.destinationStatus)
        }
    }

    @Test
    fun `a trip in progress is mapped and rounded for display`() = runTest {
        trips.setTrip(tripToCampinas)

        viewModel().state.test {
            val state = awaitItem()
            assertEquals(campinas, state.destination)
            assertEquals(Reading.Available(72), state.speedKmh)
            assertEquals(Reading.Available(296), state.rangeKm)
            assertEquals(Reading.Available(46), state.remainingKm)
            // 46 km at 75 km/h is 36.8 min, rounded up.
            assertEquals(Reading.Available(37), state.timeToArrivalMinutes)
            assertEquals(Reading.Available(1.2f), state.energyPerKm)
            // 352 - 46 * 1.2 = 296.8 kWh of 540.
            assertEquals(Reading.Available(55), state.batteryAtArrivalPercent)
            assertEquals(DestinationStatus.Reachable, state.destinationStatus)
            assertFalse(state.showsDestinationPicker)
        }
    }

    @Test
    fun `an unreachable destination gives the shortfall, rounded up`() = runTest {
        // 358 km left at 1.2 kWh/km need 429.6 kWh; 325 kWh usable cover 270.8 km.
        trips.setTrip(tripToCampinas.copy(destination = curitiba))

        viewModel().state.test {
            val state = awaitItem()
            assertEquals(DestinationStatus.Unreachable(shortByKm = 88), state.destinationStatus)
            assertEquals(Reading.NotAvailable, state.batteryAtArrivalPercent)
        }
    }

    @Test
    fun `battery draining turns a reachable destination unreachable`() = runTest {
        trips.setTrip(tripToCampinas)

        viewModel().state.test {
            assertEquals(DestinationStatus.Reachable, awaitItem().destinationStatus)

            vehicleSource.updateVehicleState { it.copy(batteryEnergyKwh = Reading.Available(60f)) }

            assertTrue(expectMostRecentItem().destinationStatus is DestinationStatus.Unreachable)
        }
    }

    @Test
    fun `picking a destination starts a trip and closes the picker`() = runTest {
        trips.setTrip(tripToCampinas)
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(TripIntent.ChangeDestinationClicked)
            assertTrue(awaitItem().isPickingDestination)

            viewModel.onIntent(TripIntent.DestinationSelected(curitiba))

            val state = expectMostRecentItem()
            assertEquals(curitiba, state.destination)
            assertEquals(Reading.Available(408), state.remainingKm)
            assertFalse(state.isPickingDestination)
        }
    }

    @Test
    fun `confirming the current destination keeps the trip progress`() = runTest {
        trips.setTrip(tripToCampinas)
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(TripIntent.ChangeDestinationClicked)
            awaitItem()

            viewModel.onIntent(TripIntent.DestinationSelected(campinas))

            assertFalse(awaitItem().isPickingDestination)
            assertEquals(tripToCampinas, trips.trip.value)
        }
    }

    @Test
    fun `dismissing the picker keeps the current trip`() = runTest {
        trips.setTrip(tripToCampinas)
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(TripIntent.ChangeDestinationClicked)
            awaitItem()

            viewModel.onIntent(TripIntent.DestinationPickerDismissed)

            val state = awaitItem()
            assertFalse(state.isPickingDestination)
            assertEquals(campinas, state.destination)
        }
    }

    @Test
    fun `destination intents are ignored while driving`() = runTest {
        trips.setTrip(tripToCampinas)
        vehicleSource.setDrivingMode(DrivingMode.Driving)
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(TripIntent.ChangeDestinationClicked)
            viewModel.onIntent(TripIntent.DestinationSelected(curitiba))

            expectNoEvents()
            assertEquals(tripToCampinas, trips.trip.value)
        }
    }

    @Test
    fun `starting to drive closes the picker, and it stays closed once parked again`() = runTest {
        trips.setTrip(tripToCampinas)
        val viewModel = viewModel()

        viewModel.state.test {
            awaitItem()
            viewModel.onIntent(TripIntent.ChangeDestinationClicked)
            assertTrue(awaitItem().showsDestinationPicker)

            vehicleSource.setDrivingMode(DrivingMode.Driving)
            val driving = expectMostRecentItem()
            assertEquals(DrivingMode.Driving, driving.drivingMode)
            assertFalse(driving.showsDestinationPicker)

            vehicleSource.setDrivingMode(DrivingMode.Parked)
            val parked = expectMostRecentItem()
            assertEquals(DrivingMode.Parked, parked.drivingMode)
            assertFalse(parked.showsDestinationPicker)
        }
    }

    @Test
    fun `unavailable vehicle data leaves trip values not available, never crashes`() = runTest {
        vehicleSource.setVehicleState(VehicleState.Unknown)
        trips.setTrip(tripToCampinas)

        viewModel().state.test {
            val state = awaitItem()
            assertEquals(Reading.NotAvailable, state.speedKmh)
            assertEquals(Reading.NotAvailable, state.energyPerKm)
            assertEquals(Reading.NotAvailable, state.batteryAtArrivalPercent)
            assertEquals(DestinationStatus.Unknown, state.destinationStatus)
            // Distance comes from the trip, so it still shows.
            assertEquals(Reading.Available(46), state.remainingKm)
        }
    }
}
