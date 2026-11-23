package com.cezila.truckcockpit.core.data.trip

import com.cezila.truckcockpit.core.domain.model.Destination

/**
 * Destinations offered on the Trip screen. The head unit has no network or navigation, so these
 * stand in for a route planner: road distances from a depot in São Paulo.
 */
internal object SimulatedDestinations {
    val all: List<Destination> = listOf(
        Destination(id = "santos", name = "Santos Port Terminal", distanceKm = 78f),
        Destination(id = "campinas", name = "Campinas Distribution Center", distanceKm = 96f),
        Destination(id = "ribeirao-preto", name = "Ribeirão Preto Hub", distanceKm = 315f),
        Destination(id = "curitiba", name = "Curitiba Logistics Park", distanceKm = 408f),
    )
}
