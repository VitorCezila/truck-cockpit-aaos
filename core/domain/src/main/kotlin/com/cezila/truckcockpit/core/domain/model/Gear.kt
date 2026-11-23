package com.cezila.truckcockpit.core.domain.model

/** Selected gear. Automatic positions plus numbered gears for manual or automated-manual gearboxes. */
sealed interface Gear {
    data object Park : Gear

    data object Reverse : Gear

    data object Neutral : Gear

    data object Drive : Gear

    data class Manual(val number: Int) : Gear {
        init {
            require(number >= 1) { "Gear number must be 1 or more, was $number" }
        }
    }
}
