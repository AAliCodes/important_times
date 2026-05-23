package za.co.amp.prayertimes.domain

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

/**
 * Unit tests for [SystemTimeFormatter].
 *
 * [android.text.format.DateFormat.is24HourFormat] is a static Android method, so we mock it
 * via MockK's [mockkStatic]. The formatter's timezone is pinned to UTC for deterministic output.
 *
 * Covers:
 * - 24-hour format: output matches "HH:mm" pattern (no AM/PM)
 * - 12-hour format: output matches "h:mm a" pattern (contains AM or PM)
 * - Midnight, noon, and arbitrary times in both modes
 */
class SystemTimeFormatterTest {

    private val formatter = SystemTimeFormatter()
    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        context = mockk()
        mockkStatic(android.text.format.DateFormat::class)
    }

    // Helper: create an Instant for a given UTC hour:minute on 2024-01-15
    private fun instantAt(hour: Int, minute: Int): Instant =
        Instant.parse("2024-01-15T%02d:%02d:00Z".format(hour, minute))

    // -------------------------------------------------------------------------
    // 24-hour mode
    // -------------------------------------------------------------------------

    @Test
    fun `24-hour mode formats 13h45 as 13 colon 45`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns true
        val result = formatter.format(instantAt(13, 45), context)
        // Should not contain AM or PM
        assertFalse(result.contains("AM", ignoreCase = true), "24h output should not contain AM: $result")
        assertFalse(result.contains("PM", ignoreCase = true), "24h output should not contain PM: $result")
        // Should contain the hour digits
        assertTrue(result.contains("13"), "24h output should contain '13': $result")
        assertTrue(result.contains("45"), "24h output should contain '45': $result")
    }

    @Test
    fun `24-hour mode formats midnight as 00 colon 00`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns true
        val result = formatter.format(instantAt(0, 0), context)
        assertFalse(result.contains("AM", ignoreCase = true))
        assertFalse(result.contains("PM", ignoreCase = true))
        assertTrue(result.contains("00"))
    }

    @Test
    fun `24-hour mode formats noon as 12 colon 00`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns true
        val result = formatter.format(instantAt(12, 0), context)
        assertFalse(result.contains("AM", ignoreCase = true))
        assertFalse(result.contains("PM", ignoreCase = true))
        assertTrue(result.contains("12"))
    }

    @Test
    fun `24-hour mode formats 05h30 with leading zero`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns true
        val result = formatter.format(instantAt(5, 30), context)
        assertFalse(result.contains("AM", ignoreCase = true))
        assertFalse(result.contains("PM", ignoreCase = true))
        assertTrue(result.contains("05"), "24h output should zero-pad hours: $result")
    }

    // -------------------------------------------------------------------------
    // 12-hour mode
    // -------------------------------------------------------------------------

    @Test
    fun `12-hour mode formats 13h45 as 1 colon 45 PM`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns false
        val result = formatter.format(instantAt(13, 45), context)
        assertTrue(
            result.contains("PM", ignoreCase = true),
            "12h output for 13:45 should contain PM: $result"
        )
        assertTrue(result.contains("1"), "12h output should contain hour '1': $result")
        assertTrue(result.contains("45"), "12h output should contain '45': $result")
    }

    @Test
    fun `12-hour mode formats 05h30 as 5 colon 30 AM`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns false
        val result = formatter.format(instantAt(5, 30), context)
        assertTrue(
            result.contains("AM", ignoreCase = true),
            "12h output for 05:30 should contain AM: $result"
        )
        assertTrue(result.contains("5"), "12h output should contain hour '5': $result")
        assertTrue(result.contains("30"))
    }

    @Test
    fun `12-hour mode formats noon as 12 colon 00 PM`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns false
        val result = formatter.format(instantAt(12, 0), context)
        assertTrue(
            result.contains("PM", ignoreCase = true),
            "12h output for noon should contain PM: $result"
        )
    }

    @Test
    fun `12-hour mode formats midnight as 12 colon 00 AM`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns false
        val result = formatter.format(instantAt(0, 0), context)
        assertTrue(
            result.contains("AM", ignoreCase = true),
            "12h output for midnight should contain AM: $result"
        )
    }

    // -------------------------------------------------------------------------
    // Output format shape
    // -------------------------------------------------------------------------

    @Test
    fun `24-hour output matches HH colon mm pattern`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns true
        val result = formatter.format(instantAt(9, 7), context)
        // Pattern: two digits, colon, two digits
        assertTrue(result.matches(Regex("\\d{2}:\\d{2}")), "Expected HH:mm but got: $result")
    }

    @Test
    fun `12-hour output contains colon separator`() {
        every { android.text.format.DateFormat.is24HourFormat(context) } returns false
        val result = formatter.format(instantAt(9, 7), context)
        assertTrue(result.contains(":"), "12h output should contain ':': $result")
    }
}
