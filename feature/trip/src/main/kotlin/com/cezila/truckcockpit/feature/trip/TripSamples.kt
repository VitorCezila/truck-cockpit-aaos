package com.cezila.truckcockpit.feature.trip

import com.cezila.truckcockpit.core.domain.model.Destination
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Gear
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripAverage

/** States for previews and snapshot tests. */
internal object TripSamples {
    private val destinations = listOf(
        Destination(id = "santos", name = "Santos Port Terminal", distanceKm = 78f),
        Destination(id = "campinas", name = "Campinas Distribution Center", distanceKm = 96f),
        Destination(id = "ribeirao-preto", name = "Ribeirão Preto Hub", distanceKm = 315f),
        Destination(id = "curitiba", name = "Curitiba Logistics Park", distanceKm = 408f),
    )

    val parked = TripState(
        drivingMode = DrivingMode.Parked,
        speedKmh = Reading.Available(0),
        gear = Reading.Available(Gear.Park),
        rangeKm = Reading.Available(296),
        destination = destinations[2],
        remainingKm = Reading.Available(186),
        timeToArrivalMinutes = TripAverage.Available(155),
        batteryAtArrivalPercent = Reading.Available(34),
        energyPerKm = TripAverage.Available(1.18f),
        destinationStatus = DestinationStatus.Reachable,
        destinations = destinations,
    )

    val driving = parked.copy(
        drivingMode = DrivingMode.Driving,
        speedKmh = Reading.Available(82),
        gear = Reading.Available(Gear.Drive),
    )

    val unreachableDriving = driving.copy(
        rangeKm = Reading.Available(158),
        batteryAtArrivalPercent = Reading.NotAvailable,
        energyPerKm = TripAverage.Available(1.42f),
        destinationStatus = DestinationStatus.Unreachable(shortByKm = 28),
    )

    val unreachableParked = unreachableDriving.copy(
        drivingMode = DrivingMode.Parked,
        speedKmh = Reading.Available(0),
        gear = Reading.Available(Gear.Park),
    )

    /** Trip just started: not enough distance for an average yet. */
    val estimatingParked = parked.copy(
        remainingKm = Reading.Available(315),
        timeToArrivalMinutes = TripAverage.Pending(remainingKm = 0.6f),
        batteryAtArrivalPercent = Reading.Available(41),
        energyPerKm = TripAverage.Pending(remainingKm = 0.6f),
    )

    val pickingDestination = parked.copy(isPickingDestination = true)

    val noDestinationParked = TripState(drivingMode = DrivingMode.Parked, destinations = destinations)

    val noDestinationDriving = TripState(drivingMode = DrivingMode.Driving, destinations = destinations)

    val notAvailableParked = TripState(
        drivingMode = DrivingMode.Parked,
        destination = destinations[1],
        remainingKm = Reading.Available(96),
        destinations = destinations,
    )

    val notAvailableDriving = notAvailableParked.copy(drivingMode = DrivingMode.Driving)
}
