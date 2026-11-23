package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.EnergyBaseline
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripProgress
import com.cezila.truckcockpit.core.domain.model.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class UpdateTripProgressUseCaseTest {

    private val update = UpdateTripProgressUseCase()
    private val start = TripProgress(Destination("cps", "Campinas", 186f))

    private fun vehicle(speedKmh: Float?, batteryKwh: Float? = null) = VehicleState(
        speedKmh = speedKmh?.let { Reading.Available(it) } ?: Reading.NotAvailable,
        batteryEnergyKwh = batteryKwh?.let { Reading.Available(it) } ?: Reading.NotAvailable,
    )

    @Test
    fun `80 kmh for one second adds about 22 meters and one second of moving time`() {
        val result = update(start, vehicle(80f), 1.seconds)

        assertEquals(80f / 3600f, result.distanceTravelledKm, 1e-5f)
        assertEquals(1.seconds, result.movingTime)
    }

    @Test
    fun `samples add up over a trip`() {
        var progress = start
        repeat(3600) { progress = update(progress, vehicle(60f), 1.seconds) }

        assertEquals(60f, progress.distanceTravelledKm, 0.05f)
        assertEquals(60.minutes, progress.movingTime)
        assertEquals(126f, progress.remainingKm, 0.05f)
    }

    @Test
    fun `standing still adds no distance and no moving time`() {
        val result = update(start, vehicle(0f), 1.seconds)

        assertEquals(0f, result.distanceTravelledKm)
        assertEquals(Duration.ZERO, result.movingTime)
    }

    @Test
    fun `unknown speed adds nothing`() {
        assertEquals(start, update(start, vehicle(null), 1.seconds))
    }

    @Test
    fun `negative speed and negative time add nothing`() {
        assertEquals(0f, update(start, vehicle(-20f), 1.seconds).distanceTravelledKm)
        assertEquals(0f, update(start, vehicle(80f), (-1).seconds).distanceTravelledKm)
    }

    @Test
    fun `a long gap between samples is capped`() {
        val result = update(start, vehicle(90f), 10.minutes)

        assertEquals(90f * 5f / 3600f, result.distanceTravelledKm, 1e-5f)
        assertEquals(UpdateTripProgressUseCase.MAX_SAMPLE_GAP, result.movingTime)
    }

    @Test
    fun `energy baseline is taken the first time the battery is known, then kept`() {
        val noBattery = update(start, vehicle(72f), 5.seconds)
        assertNull(noBattery.energyBaseline)

        val first = update(noBattery, vehicle(72f, batteryKwh = 412f), 5.seconds)
        assertEquals(EnergyBaseline(batteryKwh = 412f, distanceKm = first.distanceTravelledKm), first.energyBaseline)

        val later = update(first, vehicle(72f, batteryKwh = 400f), 5.seconds)
        assertEquals(first.energyBaseline, later.energyBaseline)
    }

    @Test
    fun `remaining distance never goes below zero`() {
        val arrived = start.copy(distanceTravelledKm = 190f)

        assertEquals(0f, arrived.remainingKm)
    }
}
