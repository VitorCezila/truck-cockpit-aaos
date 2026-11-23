package com.cezila.truckcockpit.core.domain.usecase

import com.cezila.truckcockpit.core.domain.model.Reading
import com.cezila.truckcockpit.core.domain.model.TripProgress
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Time left to the destination at the trip's average moving speed. Not available until
 * [TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM] have been driven, so the first seconds of
 * acceleration do not produce a wild estimate.
 */
class EstimateTimeToArrivalUseCase @Inject constructor() {

    operator fun invoke(progress: TripProgress): Reading<Duration> {
        if (progress.remainingKm == 0f) return Reading.Available(Duration.ZERO)
        if (progress.distanceTravelledKm < TripProgress.MIN_DISTANCE_FOR_AVERAGES_KM) return Reading.NotAvailable
        val movingHours = progress.movingTime.inWholeMilliseconds / MILLIS_PER_HOUR
        if (movingHours <= 0.0) return Reading.NotAvailable
        val averageKmh = progress.distanceTravelledKm / movingHours
        return Reading.Available((progress.remainingKm / averageKmh).hours)
    }

    private companion object {
        const val MILLIS_PER_HOUR = 3_600_000.0
    }
}
