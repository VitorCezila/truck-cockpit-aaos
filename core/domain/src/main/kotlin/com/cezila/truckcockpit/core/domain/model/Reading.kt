package com.cezila.truckcockpit.core.domain.model

/**
 * A vehicle value that may be missing: the property is not supported, its permission was denied,
 * or the vehicle reported an error. Never use a fake zero for a missing value.
 */
sealed interface Reading<out T> {
    data class Available<out T>(val value: T) : Reading<T>

    data object NotAvailable : Reading<Nothing>
}

inline fun <T, R> Reading<T>.map(transform: (T) -> R): Reading<R> = when (this) {
    is Reading.Available -> Reading.Available(transform(value))
    Reading.NotAvailable -> Reading.NotAvailable
}

fun <T> Reading<T>.valueOrNull(): T? = (this as? Reading.Available)?.value
