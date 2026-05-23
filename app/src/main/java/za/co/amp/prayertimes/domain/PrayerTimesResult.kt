package za.co.amp.prayertimes.domain

/**
 * The result of a prayer time calculation performed by [PrayerTimeCalculator].
 */
sealed class PrayerTimesResult {
    /**
     * Calculation succeeded.
     *
     * @param prayers Exactly five [PrayerEntry] items in strictly chronological order
     *   (Fajr < Dhuhr < Asr < Maghrib < Isha).
     * @param sunrise Optional sunrise entry; non-null only when the caller requested it.
     */
    data class Success(
        val prayers: List<PrayerEntry>,
        val sunrise: PrayerEntry?,
    ) : PrayerTimesResult()

    /**
     * The supplied coordinates are outside valid ranges (lat ∉ [-90,90] or lon ∉ [-180,180]).
     *
     * @param reason Human-readable description of the validation failure.
     */
    data class InvalidCoordinates(val reason: String) : PrayerTimesResult()
}
