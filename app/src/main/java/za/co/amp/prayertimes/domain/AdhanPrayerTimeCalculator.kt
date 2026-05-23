package za.co.amp.prayertimes.domain

import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.Coordinates as AdhanCoordinates

/**
 * Concrete implementation of [PrayerTimeCalculator] backed by the Adhan-Java library.
 *
 * Validates coordinates before delegating to [PrayerTimes], then maps the result back to
 * domain types. The five obligatory prayers are always returned in strictly chronological
 * order (Fajr < Dhuhr < Asr < Maghrib < Isha). An optional Sunrise entry is included when
 * [showSunrise] is `true`.
 *
 * All [PrayerEntry] items are returned with `isNext = false`; the caller is responsible for
 * annotating the next prayer via [NextPrayerCalculator].
 */
class AdhanPrayerTimeCalculator : PrayerTimeCalculator {

    override fun calculate(
        coordinates: Coordinates,
        date: DateComponents,
        method: CalculationMethod,
        showSunrise: Boolean,
    ): PrayerTimesResult {
        // Validate latitude and longitude ranges before delegating to Adhan.
        if (coordinates.latitude < -90.0 || coordinates.latitude > 90.0) {
            return PrayerTimesResult.InvalidCoordinates(
                "Latitude ${coordinates.latitude} is outside the valid range [-90, 90]."
            )
        }
        if (coordinates.longitude < -180.0 || coordinates.longitude > 180.0) {
            return PrayerTimesResult.InvalidCoordinates(
                "Longitude ${coordinates.longitude} is outside the valid range [-180, 180]."
            )
        }

        val adhanCoordinates = AdhanCoordinates(coordinates.latitude, coordinates.longitude)
        val parameters = method.toAdhanParameters()
        val prayerTimes = PrayerTimes(adhanCoordinates, date, parameters)

        // Build the five obligatory prayer entries in chronological order.
        val prayers = listOf(
            PrayerEntry(prayer = Prayer.FAJR,    time = prayerTimes.fajr.toInstant(),    isNext = false),
            PrayerEntry(prayer = Prayer.DHUHR,   time = prayerTimes.dhuhr.toInstant(),   isNext = false),
            PrayerEntry(prayer = Prayer.ASR,     time = prayerTimes.asr.toInstant(),     isNext = false),
            PrayerEntry(prayer = Prayer.MAGHRIB, time = prayerTimes.maghrib.toInstant(), isNext = false),
            PrayerEntry(prayer = Prayer.ISHA,    time = prayerTimes.isha.toInstant(),    isNext = false),
        )

        // Optionally include sunrise.
        val sunrise: PrayerEntry? = if (showSunrise) {
            PrayerEntry(prayer = Prayer.SUNRISE, time = prayerTimes.sunrise.toInstant(), isNext = false)
        } else {
            null
        }

        return PrayerTimesResult.Success(prayers = prayers, sunrise = sunrise)
    }
}
