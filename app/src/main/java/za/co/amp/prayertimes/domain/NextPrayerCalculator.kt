package za.co.amp.prayertimes.domain

import java.time.Instant

/**
 * Determines which prayer in a list is the next upcoming prayer relative to a given instant.
 *
 * This is a pure function — implementations must not have side effects.
 */
interface NextPrayerCalculator {
    /**
     * Find the index of the next prayer.
     *
     * @param prayers A list of prayer entries. The list is expected to be in chronological order.
     * @param now The current instant to compare against.
     * @return The index of the first entry whose [PrayerEntry.time] is strictly greater than [now],
     *   or `null` if all prayers have already passed (i.e., [now] is after the last prayer).
     */
    fun findNext(prayers: List<PrayerEntry>, now: Instant): Int?
}
