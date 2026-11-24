package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.TripAverage
import com.cezila.truckcockpit.core.domain.model.TripProgress
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EstimateTimeToArrivalUseCaseTest {

    private val estimate = EstimateTimeToArrivalUseCase()
    private val destination = Destination("cps", "Campinas", 186f)

    @Test
    fun `remaining distance at the average moving speed`() {
        // 66 km in 55 min is 72 km/h: the 120 km left take 100 min.
        val trip = TripProgress(destination, distanceTravelledKm = 66f, movingTime = 55.minutes)

        val result = estimate(trip) as TripAverage.Available

        assertEquals(100.minutes.inWholeSeconds.toDouble(), result.value.inWholeSeconds.toDouble(), 1.0)
    }

    @Test
    fun `pending before the minimum distance, with the distance still needed`() {
        val trip = TripProgress(destination, distanceTravelledKm = 0.25f, movingTime = 15.seconds)

        assertEquals(TripAverage.Pending(remainingKm = 0.75f), estimate(trip))
    }

    @Test
    fun `not available, not pending, without moving time past the minimum distance`() {
        val trip = TripProgress(destination, distanceTravelledKm = 5f, movingTime = Duration.ZERO)

        assertEquals(TripAverage.NotAvailable, estimate(trip))
    }

    @Test
    fun `zero once at the destination`() {
        val trip = TripProgress(destination, distanceTravelledKm = 186f, movingTime = 2.hours)

        assertEquals(TripAverage.Available(Duration.ZERO), estimate(trip))
    }

    @Test
    fun `zero, not pending, when the destination is reached before the minimum distance`() {
        val yard = Destination("yard", "Yard", 0.5f)
        val trip = TripProgress(yard, distanceTravelledKm = 0.5f, movingTime = 30.seconds)

        assertEquals(TripAverage.Available(Duration.ZERO), estimate(trip))
    }
}
