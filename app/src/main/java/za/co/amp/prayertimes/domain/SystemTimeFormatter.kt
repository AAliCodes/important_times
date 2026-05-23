package za.co.amp.prayertimes.domain

import android.content.Context
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Concrete [TimeFormatter] that respects the device's 12/24-hour system setting.
 *
 * - 24-hour devices: formats as "HH:mm" (e.g., "13:45")
 * - 12-hour devices: formats as "h:mm a" (e.g., "1:45 PM")
 *
 * The [Instant] is converted to the device's default timezone before formatting.
 */
class SystemTimeFormatter : TimeFormatter {

    override fun format(time: Instant, context: Context): String {
        val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
        val pattern = if (is24Hour) "HH:mm" else "h:mm a"
        val formatter = DateTimeFormatter.ofPattern(pattern)
        val zonedDateTime = time.atZone(ZoneId.systemDefault())
        return formatter.format(zonedDateTime)
    }
}
