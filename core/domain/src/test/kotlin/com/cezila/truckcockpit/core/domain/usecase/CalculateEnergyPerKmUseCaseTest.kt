package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.EnergyBaseline
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateEnergyPerKmUseCaseTest {

    private val calculate = CalculateEnergyPerKmUseCase()
    private val destination = Destination("cps", "Campinas", 186f)

    private fun progress(distanceKm: Float, baseline: EnergyBaseline?) =
        TripProgress(destination, distanceTravelledKm = distanceKm, energyBaseline = baseline)

    @Test
    fun `energy used over distance driven, 1_2 kWh per km for a loaded truck`() {
        val trip = progress(distanceKm = 50f, baseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f))

        val result = calculate(trip, Reading.Available(352f))

        assertEquals(1.2f, (result as Reading.Available).value, 1e-4f)
    }

    @Test
    fun `distance before the baseline does not count`() {
        val trip = progress(distanceKm = 30f, baseline = EnergyBaseline(batteryKwh = 400f, distanceKm = 10f))

        val result = calculate(trip, Reading.Available(376f))

        assertEquals(1.2f, (result as Reading.Available).value, 1e-4f)
    }

    @Test
    fun `not available below the minimum distance`() {
        val trip = progress(distanceKm = 0.9f, baseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f))

        assertEquals(Reading.NotAvailable, calculate(trip, Reading.Available(411f)))
    }

    @Test
    fun `available at exactly the minimum distance`() {
        val trip = progress(distanceKm = 1f, baseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f))

        assertEquals(Reading.Available(1f), calculate(trip, Reading.Available(411f)))
    }

    @Test
    fun `not available without a baseline`() {
        assertEquals(Reading.NotAvailable, calculate(progress(50f, baseline = null), Reading.Available(352f)))
    }

    @Test
    fun `not available without the current battery level`() {
        val trip = progress(distanceKm = 50f, baseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f))

        assertEquals(Reading.NotAvailable, calculate(trip, Reading.NotAvailable))
    }

    @Test
    fun `net energy gained downhill gives zero, never negative`() {
        val trip = progress(distanceKm = 5f, baseline = EnergyBaseline(batteryKwh = 400f, distanceKm = 0f))

        assertEquals(Reading.Available(0f), calculate(trip, Reading.Available(402f)))
    }
}
