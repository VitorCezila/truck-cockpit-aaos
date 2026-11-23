package com.cezila.truckcockpit.core.domain.model

/** Whether the battery is expected to get the truck to its destination. */
sealed interface Reachability {
    /** Not enough data: no battery level, or no way to estimate energy use. */
    data object Unknown : Reachability

    /** Arrives with [batteryAtArrivalKwh] left, at or above the reserve. */
    data class Reachable(val batteryAtArrivalKwh: Float) : Reachability

    /** The battery reaches its reserve [shortByKm] before the destination. */
    data class Unreachable(val shortByKm: Float) : Reachability
}
