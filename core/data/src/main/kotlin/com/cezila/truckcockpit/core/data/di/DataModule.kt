package com.cezila.truckcockpit.core.data.di

import com.cezila.truckcockpit.core.data.car.CarVehicleDataSource
import com.cezila.truckcockpit.core.data.trip.TrackingTripRepository
import com.cezila.truckcockpit.core.domain.trip.TripRepository
import com.cezila.truckcockpit.core.domain.vehicle.VehicleDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {
    @Binds
    fun bindVehicleDataSource(impl: CarVehicleDataSource): VehicleDataSource

    @Binds
    fun bindTripRepository(impl: TrackingTripRepository): TripRepository
}
