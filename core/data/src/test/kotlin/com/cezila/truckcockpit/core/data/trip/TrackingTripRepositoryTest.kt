package com.cezila.truckcockpit.core.data.trip

import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.usecase.UpdateTripProgressUseCase
import com.cezila.truckcockpit.core.domain.vehicle.FakeVehicleDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingTripRepositoryTest {

    private val campinas = SimulatedDestinations.all.first { it.id == "campinas" }
    private val santos = SimulatedDestinations.all.first { it.id == "santos" }

    private val vehicle = FakeVehicleDataSource(
        initialState = VehicleState(
            speedKmh = Reading.Available(72f),
            batteryEnergyKwh = Reading.Available(412f),
        ),
    )

    private fun TestScope.repository() =
        TrackingTripRepository(vehicle, UpdateTripProgressUseCase(), backgroundScope)

    @Test
    fun `no trip until a destination is picked`() = runTest {
        assertNull(repository().trip.value)
    }

    @Test
    fun `distance grows with time at a steady speed`() = runTest {
        val repository = repository()

        repository.startTrip(campinas)
        runCurrent()
        assertEquals(0f, repository.trip.value!!.distanceTravelledKm)

        // 72 km/h is 20 m/s: 50 s cover 1 km, though the vehicle state never changes.
        advanceTimeBy(50.seconds)
        runCurrent()

        val trip = repository.trip.value!!
        assertEquals(campinas, trip.destination)
        assertEquals(1f, trip.distanceTravelledKm, 1e-3f)
        assertEquals(50.seconds, trip.movingTime)
        assertEquals(412f, trip.energyBaseline?.batteryKwh)
    }

    @Test
    fun `speed changes are picked up`() = runTest {
        val repository = repository()
        repository.startTrip(campinas)
        runCurrent()

        vehicle.updateVehicleState { it.copy(speedKmh = Reading.Available(0f)) }
        advanceTimeBy(30.seconds)
        runCurrent()

        assertEquals(0f, repository.trip.value!!.distanceTravelledKm)
    }

    @Test
    fun `a new destination restarts the trip from zero`() = runTest {
        val repository = repository()
        repository.startTrip(campinas)
        advanceTimeBy(100.seconds)
        runCurrent()

        repository.startTrip(santos)
        runCurrent()

        val trip = repository.trip.value!!
        assertEquals(santos, trip.destination)
        assertEquals(0f, trip.distanceTravelledKm)

        advanceTimeBy(50.seconds)
        runCurrent()
        assertEquals(1f, repository.trip.value!!.distanceTravelledKm, 1e-3f)
    }

    @Test
    fun `tracking stops at the destination and releases the vehicle data`() = runTest {
        val repository = repository()
        // 78 km at 72 km/h take 65 min.
        repository.startTrip(santos)
        advanceTimeBy(66.minutes)
        runCurrent()

        assertEquals(0f, repository.trip.value!!.remainingKm)
        assertEquals(0, vehicle.vehicleStateSubscribers.value)
    }

    @Test
    fun `destinations are the simulated list`() = runTest {
        assertEquals(SimulatedDestinations.all, repository().destinations)
    }
}
