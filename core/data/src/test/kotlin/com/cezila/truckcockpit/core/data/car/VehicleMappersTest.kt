package com.cezila.truckcockpit.core.data.car

import com.cezila.truckcockpit.core.domain.model.Gear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleMappersTest {

    @Test
    fun `speed converts m per s to km per h`() {
        assertEquals(79.92f, VehicleMappers.speedKmh(22.2f), 0.001f)
    }

    @Test
    fun `reverse speed is shown as positive`() {
        assertEquals(18f, VehicleMappers.speedKmh(-5f), 0.001f)
    }

    @Test
    fun `energy converts Wh to kWh`() {
        assertEquals(412f, VehicleMappers.kwh(412_000f), 0.001f)
    }

    @Test
    fun `range converts meters to km`() {
        assertEquals(120f, VehicleMappers.km(120_000f), 0.001f)
    }

    @Test
    fun `automatic gears map to their domain values`() {
        assertEquals(Gear.Neutral, VehicleMappers.gear(0x0001))
        assertEquals(Gear.Reverse, VehicleMappers.gear(0x0002))
        assertEquals(Gear.Park, VehicleMappers.gear(0x0004))
        assertEquals(Gear.Drive, VehicleMappers.gear(0x0008))
    }

    @Test
    fun `numbered gears map to their number`() {
        assertEquals(Gear.Manual(1), VehicleMappers.gear(0x0010))
        assertEquals(Gear.Manual(2), VehicleMappers.gear(0x0020))
        assertEquals(Gear.Manual(9), VehicleMappers.gear(0x1000))
    }

    @Test
    fun `unknown and invalid gear values map to null`() {
        assertNull(VehicleMappers.gear(0)) // GEAR_UNKNOWN
        assertNull(VehicleMappers.gear(0x0030)) // two bits set
        assertNull(VehicleMappers.gear(0x2000)) // above GEAR_9
        assertNull(VehicleMappers.gear(-1))
    }
}
