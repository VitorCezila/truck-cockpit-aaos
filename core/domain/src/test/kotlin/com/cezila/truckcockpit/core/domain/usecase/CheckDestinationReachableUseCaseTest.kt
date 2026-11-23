package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Reachability
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckDestinationReachableUseCaseTest {

    private val check = CheckDestinationReachableUseCase()

    /** 540 kWh pack by default: the reserve is 27 kWh. */
    private fun vehicle(batteryKwh: Float?, rangeKm: Float? = null, capacityKwh: Float? = 540f) = VehicleState(
        batteryEnergyKwh = batteryKwh?.let { Reading.Available(it) } ?: Reading.NotAvailable,
        batteryCapacityKwh = capacityKwh?.let { Reading.Available(it) } ?: Reading.NotAvailable,
        rangeKm = rangeKm?.let { Reading.Available(it) } ?: Reading.NotAvailable,
    )

    @Test
    fun `reachable with the trip average, arrival energy is what is left`() {
        val result = check(remainingKm = 186f, vehicle(batteryKwh = 412f), tripEnergyPerKm = Reading.Available(1.2f))

        assertEquals(412f - 186f * 1.2f, (result as Reachability.Reachable).batteryAtArrivalKwh, 1e-3f)
    }

    @Test
    fun `unreachable gives the distance short of the destination`() {
        // Usable 127 - 27 = 100 kWh at 1.25 kWh/km covers 80 km of 108.
        val result = check(remainingKm = 108f, vehicle(batteryKwh = 127f), tripEnergyPerKm = Reading.Available(1.25f))

        assertEquals(28f, (result as Reachability.Unreachable).shortByKm, 1e-3f)
    }

    @Test
    fun `arriving with exactly the reserve is reachable`() {
        val result = check(remainingKm = 100f, vehicle(batteryKwh = 152f), tripEnergyPerKm = Reading.Available(1.25f))

        assertEquals(27f, (result as Reachability.Reachable).batteryAtArrivalKwh, 1e-3f)
    }

    @Test
    fun `without a trip average the vehicle range sets the energy use`() {
        // 412 kWh over 296 km is ~1.39 kWh/km: 186 km is reachable, 400 km is not.
        val vehicle = vehicle(batteryKwh = 412f, rangeKm = 296f)

        assertTrue(check(186f, vehicle, Reading.NotAvailable) is Reachability.Reachable)
        val far = check(400f, vehicle, Reading.NotAvailable) as Reachability.Unreachable
        // The reserve takes 27 kWh, ~19.4 km of range: 400 - (296 - 19.4).
        assertEquals(123.4f, far.shortByKm, 0.1f)
    }

    @Test
    fun `a zero trip average falls back to the vehicle range`() {
        val result = check(500f, vehicle(batteryKwh = 412f, rangeKm = 296f), Reading.Available(0f))

        assertTrue(result is Reachability.Unreachable)
    }

    @Test
    fun `unknown without a battery level`() {
        val result = check(186f, vehicle(batteryKwh = null, rangeKm = 296f), Reading.Available(1.2f))

        assertEquals(Reachability.Unknown, result)
    }

    @Test
    fun `unknown without any energy use estimate`() {
        assertEquals(Reachability.Unknown, check(186f, vehicle(batteryKwh = 412f), Reading.NotAvailable))
        assertEquals(Reachability.Unknown, check(186f, vehicle(batteryKwh = 412f, rangeKm = 0f), Reading.NotAvailable))
    }

    @Test
    fun `without the capacity there is no reserve`() {
        val result = check(96f, vehicle(batteryKwh = 120f, capacityKwh = null), Reading.Available(1.25f))

        assertEquals(0f, (result as Reachability.Reachable).batteryAtArrivalKwh, 1e-3f)
    }

    @Test
    fun `an empty battery covers no distance, even without an energy estimate`() {
        assertEquals(Reachability.Unreachable(186f), check(186f, vehicle(batteryKwh = 0f), Reading.NotAvailable))
    }

    @Test
    fun `already at the destination is reachable whatever the battery`() {
        assertEquals(Reachability.Reachable(10f), check(0f, vehicle(batteryKwh = 10f), Reading.NotAvailable))
        assertEquals(Reachability.Reachable(500f), check(0f, vehicle(batteryKwh = 500f), Reading.Available(1.2f)))
    }
}
