package com.cezila.truckcockpit.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleStateTest {

    @Test
    fun `battery percent is energy over capacity, rounded`() {
        val state = VehicleState(
            batteryEnergyKwh = Reading.Available(412f),
            batteryCapacityKwh = Reading.Available(540f),
        )

        assertEquals(Reading.Available(76), state.batteryPercent)
    }

    @Test
    fun `battery percent is clamped to 0-100`() {
        val state = VehicleState(
            batteryEnergyKwh = Reading.Available(600f),
            batteryCapacityKwh = Reading.Available(540f),
        )

        assertEquals(Reading.Available(100), state.batteryPercent)
    }

    @Test
    fun `battery percent is not available without capacity`() {
        val state = VehicleState(batteryEnergyKwh = Reading.Available(412f))

        assertEquals(Reading.NotAvailable, state.batteryPercent)
    }

    @Test
    fun `battery percent is not available when capacity is zero`() {
        val state = VehicleState(
            batteryEnergyKwh = Reading.Available(412f),
            batteryCapacityKwh = Reading.Available(0f),
        )

        assertEquals(Reading.NotAvailable, state.batteryPercent)
    }

    @Test
    fun `battery percent is not available without energy`() {
        val state = VehicleState(batteryCapacityKwh = Reading.Available(540f))

        assertEquals(Reading.NotAvailable, state.batteryPercent)
    }
}
