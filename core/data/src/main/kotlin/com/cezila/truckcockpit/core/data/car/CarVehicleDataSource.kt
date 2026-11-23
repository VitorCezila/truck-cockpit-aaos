package com.cezila.truckcockpit.core.data.car

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.drivingstate.CarUxRestrictions
import android.car.drivingstate.CarUxRestrictionsManager
import android.car.hardware.CarPropertyConfig
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import com.cezila.truckcockpit.core.data.di.ApplicationScope
import com.cezila.truckcockpit.core.data.di.DefaultDispatcher
import com.cezila.truckcockpit.core.data.flow.shareLatest
import com.cezila.truckcockpit.core.data.flow.throttleLatest
import com.cezila.truckcockpit.core.domain.model.DrivingMode
import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.VehicleState
import com.cezila.truckcockpit.core.domain.model.map
import com.cezila.truckcockpit.core.domain.vehicle.VehicleDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [VehicleDataSource] backed by `CarPropertyManager` and `CarUxRestrictionsManager`.
 *
 * Each property degrades on its own: unsupported, permission denied, an error event or a failed
 * car service call all become [Reading.NotAvailable] for that value only. Nothing here throws to
 * the caller. Subscriptions are rebuilt whenever the car service reconnects.
 *
 * The Car API throws several unrelated RuntimeExceptions (SecurityException,
 * PropertyNotAvailableException, CarInternalErrorException, IllegalArgumentException), so calls
 * into it catch RuntimeException as a whole.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
internal class CarVehicleDataSource @Inject constructor(
    connection: CarConnection,
    @ApplicationScope scope: CoroutineScope,
    @DefaultDispatcher dispatcher: CoroutineDispatcher,
) : VehicleDataSource {

    // Shared so every screen reuses one set of callbacks; CarUxRestrictionsManager also accepts
    // only one listener per manager.
    override val vehicleState: Flow<VehicleState> = connection.car
        .flatMapLatest { car ->
            val manager = car?.manager<CarPropertyManager>(Car.PROPERTY_SERVICE)
            if (manager == null) flowOf(VehicleState.Unknown) else vehicleState(manager)
        }
        .distinctUntilChanged()
        .flowOn(dispatcher)
        .shareLatest(scope)

    override val drivingMode: Flow<DrivingMode> = connection.car
        .flatMapLatest { car ->
            val manager = car?.manager<CarUxRestrictionsManager>(Car.CAR_UX_RESTRICTION_SERVICE)
            // Disconnected means unknown: assume the restrictive state.
            if (manager == null) flowOf(DrivingMode.Driving) else drivingMode(manager)
        }
        .distinctUntilChanged()
        .flowOn(dispatcher)
        .shareLatest(scope)

    private fun vehicleState(manager: CarPropertyManager): Flow<VehicleState> = combine(
        manager.floatProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED, SPEED_RATE_HZ)
            .map { reading -> reading.map(VehicleMappers::speedKmh) }
            .throttleLatest(SPEED_THROTTLE_MILLIS),
        manager.intProperty(VehiclePropertyIds.GEAR_SELECTION, SLOW_RATE_HZ)
            .map { reading -> reading.map(VehicleMappers::gear).orNotAvailable() },
        manager.floatProperty(VehiclePropertyIds.EV_BATTERY_LEVEL, SLOW_RATE_HZ)
            .map { reading -> reading.map(VehicleMappers::kwh) },
        manager.staticFloatProperty(VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY)
            .map { reading -> reading.map(VehicleMappers::kwh) },
        manager.floatProperty(VehiclePropertyIds.RANGE_REMAINING, SLOW_RATE_HZ)
            .map { reading -> reading.map(VehicleMappers::km) },
    ) { speed, gear, batteryEnergy, batteryCapacity, range ->
        VehicleState(
            speedKmh = speed,
            gear = gear,
            batteryEnergyKwh = batteryEnergy,
            batteryCapacityKwh = batteryCapacity,
            rangeKm = range,
        )
    }

    /**
     * The listener and the initial read both write to one state, and the initial read only fills
     * it if no event came first, so an older snapshot never overwrites a newer event.
     */
    private fun drivingMode(manager: CarUxRestrictionsManager): Flow<DrivingMode> = flow {
        val latest = MutableStateFlow<DrivingMode?>(null)
        manager.registerListener { restrictions -> latest.value = restrictions.toDrivingMode() }
        try {
            val current = try {
                manager.currentCarUxRestrictions?.toDrivingMode()
            } catch (_: RuntimeException) {
                null
            }
            latest.compareAndSet(null, current ?: DrivingMode.Driving)
            emitAll(latest.filterNotNull())
        } finally {
            try {
                manager.unregisterListener()
            } catch (_: RuntimeException) {
                // The car service is gone; there is nothing left to unregister from.
            }
        }
    }.catch { emit(DrivingMode.Driving) }

    private fun CarPropertyManager.floatProperty(propertyId: Int, targetRateHz: Float): Flow<Reading<Float>> =
        property(propertyId, targetRateHz) { it as? Float }

    private fun CarPropertyManager.intProperty(propertyId: Int, targetRateHz: Float): Flow<Reading<Int>> =
        property(propertyId, targetRateHz) { it as? Int }

    /**
     * Subscribes to one property. The current value arrives as the first event; until then, and
     * whenever the property is missing, denied or in error, the reading is [Reading.NotAvailable].
     */
    private fun <T : Any> CarPropertyManager.property(
        propertyId: Int,
        targetRateHz: Float,
        cast: (Any?) -> T?,
    ): Flow<Reading<T>> = callbackFlow {
        // The config is null when the vehicle does not support the property or the permission is denied.
        val config = configOrNull(propertyId)
        if (config == null) {
            send(Reading.NotAvailable)
            awaitClose()
            return@callbackFlow
        }
        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>) {
                trySend(value.toReading(cast))
            }

            override fun onErrorEvent(propertyId: Int, areaId: Int) {
                trySend(Reading.NotAvailable)
            }
        }
        val registered = try {
            @Suppress("DEPRECATION") // subscribePropertyEvents needs API 35; minSdk is 33.
            registerCallback(callback, propertyId, config.rateFor(targetRateHz))
        } catch (_: RuntimeException) {
            false
        }
        if (!registered) send(Reading.NotAvailable)
        awaitClose {
            if (registered) {
                try {
                    unregisterCallback(callback, propertyId)
                } catch (_: RuntimeException) {
                    // The car service is gone; there is nothing left to unregister from.
                }
            }
        }
    }
        .onStart { emit(Reading.NotAvailable) }
        .catch { emit(Reading.NotAvailable) }
        .distinctUntilChanged()

    /** STATIC properties (battery capacity) never change: read once. */
    private fun CarPropertyManager.staticFloatProperty(propertyId: Int): Flow<Reading<Float>> = flow {
        val value = try {
            if (configOrNull(propertyId) != null) getFloatProperty(propertyId, GLOBAL_AREA_ID) else null
        } catch (_: RuntimeException) {
            null
        }
        emit(value?.let { Reading.Available(it) } ?: Reading.NotAvailable)
    }

    private fun CarPropertyManager.configOrNull(propertyId: Int): CarPropertyConfig<*>? = try {
        getCarPropertyConfig(propertyId)
    } catch (_: RuntimeException) {
        null
    }

    private inline fun <reified M> Car.manager(name: String): M? = try {
        getCarManager(name) as? M
    } catch (_: RuntimeException) {
        null
    }

    private fun CarPropertyConfig<*>.rateFor(targetHz: Float): Float =
        if (changeMode == CarPropertyConfig.VEHICLE_PROPERTY_CHANGE_MODE_CONTINUOUS &&
            minSampleRate <= maxSampleRate
        ) {
            targetHz.coerceIn(minSampleRate, maxSampleRate)
        } else {
            CarPropertyManager.SENSOR_RATE_ONCHANGE
        }

    private fun <T : Any> CarPropertyValue<*>.toReading(cast: (Any?) -> T?): Reading<T> =
        if (status == CarPropertyValue.STATUS_AVAILABLE) {
            cast(value)?.let { Reading.Available(it) } ?: Reading.NotAvailable
        } else {
            Reading.NotAvailable
        }

    private fun CarUxRestrictions.toDrivingMode(): DrivingMode =
        if (isRequiresDistractionOptimization) DrivingMode.Driving else DrivingMode.Parked

    private fun <T : Any> Reading<T?>.orNotAvailable(): Reading<T> = when (this) {
        is Reading.Available -> value?.let { Reading.Available(it) } ?: Reading.NotAvailable
        Reading.NotAvailable -> Reading.NotAvailable
    }

    private companion object {
        const val GLOBAL_AREA_ID = 0

        /** Ask the vehicle for ~5 speed updates/s, then let at most 4/s reach the UI. */
        const val SPEED_RATE_HZ = 5f
        const val SPEED_THROTTLE_MILLIS = 250L

        /** Gear, battery and range change slowly. */
        const val SLOW_RATE_HZ = 1f
    }
}
