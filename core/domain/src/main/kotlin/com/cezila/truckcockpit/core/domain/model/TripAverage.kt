package com.cezila.truckcockpit.core.domain.model

/**
 * A value averaged over the trip (energy use, average speed). Unlike [Reading], it can be missing for
 * two different reasons, and the driver should be told which one.
 */
sealed interface TripAverage<out T> {
    data class Available<out T>(val value: T) : TripAverage<T>

    /**
     * The data is there, but the trip has not covered [TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM] yet.
     * [remainingKm] is the distance still needed.
     */
    data class Pending(val remainingKm: Float) : TripAverage<Nothing>

    /** The vehicle data it needs is missing. */
    data object NotAvailable : TripAverage<Nothing>
}

inline fun <T, R> TripAverage<T>.map(transform: (T) -> R): TripAverage<R> = when (this) {
    is TripAverage.Available -> TripAverage.Available(transform(value))
    is TripAverage.Pending -> this
    TripAverage.NotAvailable -> TripAverage.NotAvailable
}

fun <T> TripAverage<T>.valueOrNull(): T? = (this as? TripAverage.Available)?.value

/** For consumers that only care whether there is a value. */
fun <T> TripAverage<T>.toReading(): Reading<T> = when (this) {
    is TripAverage.Available -> Reading.Available(value)
    is TripAverage.Pending, TripAverage.NotAvailable -> Reading.NotAvailable
}
