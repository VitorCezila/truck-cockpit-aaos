package com.cezila.truckcockpit.core.domain.model

/** Whether the UI must be distraction optimized. Comes from the vehicle's UX restrictions, not from speed. */
enum class DrivingMode {
    /** No UX restrictions: full layout. */
    Parked,

    /** UX restrictions require distraction optimization: reduced layout. */
    Driving,
}
