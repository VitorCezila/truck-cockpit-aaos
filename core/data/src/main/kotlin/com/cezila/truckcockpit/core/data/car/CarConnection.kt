package com.cezila.truckcockpit.core.data.car

import android.car.Car
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One connection to the car service for the whole app, opened on first use.
 *
 * [car] is null while disconnected (not connected yet, car service restarting, or no car service
 * at all). The car service may restart while the app runs: the listener reports it and the
 * connection comes back on its own, so collectors resubscribe whenever a new [Car] arrives.
 */
@Singleton
internal class CarConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val started = AtomicBoolean(false)
    private val _car = MutableStateFlow<Car?>(null)

    val car: StateFlow<Car?>
        get() {
            connectOnce()
            return _car.asStateFlow()
        }

    private fun connectOnce() {
        if (!started.compareAndSet(false, true)) return
        try {
            // DO_NOT_WAIT returns at once; the listener fires on connect, on service death and on reconnect.
            Car.createCar(context, null, Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT) { car, ready ->
                _car.value = if (ready) car else null
            }
        } catch (_: RuntimeException) {
            // No car service on this device: stay disconnected, every value is "not available".
        }
    }
}
