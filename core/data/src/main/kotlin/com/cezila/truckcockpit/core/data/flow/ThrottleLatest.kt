package com.cezila.truckcockpit.core.data.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.transform

/**
 * Emits the first value right away, then at most one value per [periodMillis], always the latest.
 * Unlike `sample`, the first value is not delayed and the last value is never dropped.
 */
internal fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> =
    conflate().transform {
        emit(it)
        delay(periodMillis)
    }
