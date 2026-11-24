package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.EnergyBaseline
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripAverage
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

        assertEquals(1.2f, (result as TripAverage.Available).value, 1e-4f)
    }

    @Test
    fun `distance before the baseline does not count`() {
        val trip = progress(distanceKm = 30f, baseline = EnergyBaseline(batteryKwh = 400f, distanceKm = 10f))

        val result = calculate(trip, Reading.Available(376f))

        assertEquals(1.2f, (result as TripAverage.Available).value, 1e-4f)
    }

    @Test
    fun `pending below the minimum distance, with the distance still needed`() {
        val trip = progress(distanceKm = 0.75f, baseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f))

        assertEquals(TripAverage.Pending(remainingKm = 0.25f), calculate(trip, Reading.Available(411f)))
    }

    @Test
    fun `the minimum distance counts from the baseline, not the trip start`() {
        // Battery first read at 49.5 km.
        val trip = progress(distanceKm = 50f, baseline = EnergyBaseline(batteryKwh = 352.5f, distanceKm = 49.5f))

        assertEquals(TripAverage.Pending(remainingKm = 0.5f), calculate(trip, Reading.Available(352f)))
    }

    @Test
    fun `available at exactly the minimum distance`() {
        val trip = progress(distanceKm = 1f, baseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f))

        assertEquals(TripAverage.Available(1f), calculate(trip, Reading.Available(411f)))
    }

    @Test
    fun `pending while the battery is readable but the trip has no baseline yet`() {
        val result = calculate(progress(50f, baseline = null), Reading.Available(352f))

        assertEquals(TripAverage.Pending(remainingKm = TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM), result)
    }

    @Test
    fun `not available without the current battery level`() {
        val trip = progress(distanceKm = 50f, baseline = EnergyBaseline(batteryKwh = 412f, distanceKm = 0f))

        assertEquals(TripAverage.NotAvailable, calculate(trip, Reading.NotAvailable))
    }

    @Test
    fun `without the battery level it is not available, not pending, even below the minimum distance`() {
        assertEquals(TripAverage.NotAvailable, calculate(progress(0.5f, baseline = null), Reading.NotAvailable))
    }

    @Test
    fun `net energy gained downhill gives zero, never negative`() {
        val trip = progress(distanceKm = 5f, baseline = EnergyBaseline(batteryKwh = 400f, distanceKm = 0f))

        assertEquals(TripAverage.Available(0f), calculate(trip, Reading.Available(402f)))
    }
}
