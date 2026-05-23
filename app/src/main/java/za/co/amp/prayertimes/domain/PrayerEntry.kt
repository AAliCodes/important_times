package za.co.amp.prayertimes.domain

import java.time.Instant

/**
 * A single prayer with its computed time and whether it is the next upcoming prayer.
 *
 * @param prayer The prayer identifier.
 * @param time The UTC instant at which this prayer begins. Formatted for display by [TimeFormatter].
 * @param isNext True if this is the next upcoming prayer relative to the current device time.
 */
data class PrayerEntry(
    val prayer: Prayer,
    val time: Instant,
    val isNext: Boolean,
)
