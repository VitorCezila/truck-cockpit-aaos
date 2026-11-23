package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.Reading
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

        val result = estimate(trip) as Reading.Available

        assertEquals(100.minutes.inWholeSeconds.toDouble(), result.value.inWholeSeconds.toDouble(), 1.0)
    }

    @Test
    fun `not available before the minimum distance`() {
        val trip = TripProgress(destination, distanceTravelledKm = 0.5f, movingTime = 30.seconds)

        assertEquals(Reading.NotAvailable, estimate(trip))
    }

    @Test
    fun `not available without moving time`() {
        val trip = TripProgress(destination, distanceTravelledKm = 5f, movingTime = Duration.ZERO)

        assertEquals(Reading.NotAvailable, estimate(trip))
    }

    @Test
    fun `zero once at the destination`() {
        val trip = TripProgress(destination, distanceTravelledKm = 186f, movingTime = 2.hours)

        assertEquals(Reading.Available(Duration.ZERO), estimate(trip))
    }
}
