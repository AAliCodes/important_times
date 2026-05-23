package za.co.amp.prayertimes.domain

import java.time.Instant

/**
 * Default implementation of [NextPrayerCalculator].
 *
 * Iterates the sorted [PrayerEntry] list and returns the index of the first entry
 * whose [PrayerEntry.time] is strictly greater than [now], or `null` if all prayers
 * have already passed.
 */
class DefaultNextPrayerCalculator : NextPrayerCalculator {

    override fun findNext(prayers: List<PrayerEntry>, now: Instant): Int? {
        for ((index, entry) in prayers.withIndex()) {
            if (entry.time > now) {
                return index
            }
        }
        return null
    }
}
