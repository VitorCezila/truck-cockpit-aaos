package com.cezila.truckcockpit.core.data.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

/**
 * Shares one upstream collection while anyone collects, replaying the latest value to late
 * subscribers. Once the last subscriber leaves, the replayed value is dropped: vehicle data is only
 * valid while it is being observed, so a screen coming back must wait for a fresh value instead of
 * seeing an old one (e.g. Parked after the truck started moving).
 */
internal fun <T> Flow<T>.shareLatest(scope: CoroutineScope): SharedFlow<T> =
    shareIn(scope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)
