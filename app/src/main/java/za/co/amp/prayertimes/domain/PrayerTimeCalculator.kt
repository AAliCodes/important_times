package za.co.amp.prayertimes.domain

import com.batoulapps.adhan.data.DateComponents

/**
 * Computes Islamic prayer times for a given location, date, and calculation method.
 *
 * Implementations must validate coordinates before delegating to the underlying library.
 */
interface PrayerTimeCalculator {
    /**
     * Calculate prayer times.
     *
     * @param coordinates Geographic coordinates of the location.
     * @param date The date for which to compute prayer times.
     * @param method The calculation method (angles and offsets) to use.
     * @param showSunrise Whether to include a Sunrise entry in the result.
     * @return [PrayerTimesResult.Success] with five entries in chronological order, or
     *   [PrayerTimesResult.InvalidCoordinates] if the coordinates are out of range.
     */
    fun calculate(
        coordinates: Coordinates,
        date: DateComponents,
        method: CalculationMethod,
        showSunrise: Boolean,
    ): PrayerTimesResult
}
