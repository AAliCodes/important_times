package za.co.amp.prayertimes.domain

import android.content.Context
import java.time.Instant

/**
 * Formats a time instant to a human-readable string that respects the device's 12/24-hour setting.
 */
interface TimeFormatter {
    /**
     * Format [time] as a display string.
     *
     * - On devices configured for 24-hour format: returns "HH:mm" (e.g., "13:45").
     * - On devices configured for 12-hour format: returns "h:mm a" (e.g., "1:45 PM").
     *
     * @param time The UTC instant to format.
     * @param context Android context used to read the device's time format setting.
     * @return A formatted time string.
     */
    fun format(time: Instant, context: Context): String
}
