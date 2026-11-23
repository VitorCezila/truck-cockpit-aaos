package com.cezila.truckcockpit.core.data.car

import com.cezila.truckcockpit.core.domain.model.Gear
import kotlin.math.abs

/** Pure conversions from VHAL units and raw values to domain units. */
internal object VehicleMappers {

    /** `PERF_VEHICLE_SPEED` is m/s and negative when reversing; the driver sees km/h. */
    fun speedKmh(metersPerSecond: Float): Float = abs(metersPerSecond) * 3.6f

    /** `EV_BATTERY_LEVEL` and `INFO_EV_BATTERY_CAPACITY` are Wh. */
    fun kwh(wattHours: Float): Float = wattHours / 1_000f

    /** `RANGE_REMAINING` is meters. */
    fun km(meters: Float): Float = meters / 1_000f

    /** `GEAR_SELECTION` is a `VehicleGear` bit value. Returns null for `GEAR_UNKNOWN` or unrecognized values. */
    fun gear(raw: Int): Gear? = when (raw) {
        VehicleGear.NEUTRAL -> Gear.Neutral
        VehicleGear.REVERSE -> Gear.Reverse
        VehicleGear.PARK -> Gear.Park
        VehicleGear.DRIVE -> Gear.Drive
        else -> VehicleGear.manualNumber(raw)?.let(Gear::Manual)
    }
}

/**
 * `android.hardware.automotive.vehicle.VehicleGear` values. That class is not part of the public
 * car library, so the values are copied here.
 */
internal object VehicleGear {
    const val NEUTRAL = 0x0001
    const val REVERSE = 0x0002
    const val PARK = 0x0004
    const val DRIVE = 0x0008
    private const val GEAR_1 = 0x0010
    private const val GEAR_9 = 0x1000

    /** GEAR_1..GEAR_9 are single bits from 0x0010 to 0x1000. */
    fun manualNumber(raw: Int): Int? {
        if (raw < GEAR_1 || raw > GEAR_9 || raw and (raw - 1) != 0) return null
        return Integer.numberOfTrailingZeros(raw) - Integer.numberOfTrailingZeros(GEAR_1) + 1
    }
}
