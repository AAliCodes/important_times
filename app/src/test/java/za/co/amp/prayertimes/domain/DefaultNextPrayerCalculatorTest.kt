package za.co.amp.prayertimes.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for [DefaultNextPrayerCalculator].
 *
 * Covers:
 * - Returns correct index when now is before all prayers
 * - Returns correct index when now is between prayers
 * - Returns null when now is after all prayers (post-Isha)
 * - Boundary: now exactly at a prayer time (not strictly greater → skip to next)
 * - Empty list returns null
 * - Single-element list
 */
class DefaultNextPrayerCalculatorTest {

    private val calculator = DefaultNextPrayerCalculator()

    // Helper to build a PrayerEntry at a given epoch second offset from a base time
    private val base = Instant.parse("2024-01-15T00:00:00Z")

    private fun entry(prayer: Prayer, offsetSeconds: Long) =
        PrayerEntry(prayer = prayer, time = base.plusSeconds(offsetSeconds), isNext = false)

    // Five prayers spaced 2 hours apart starting at base
    private val fivePrayers = listOf(
        entry(Prayer.FAJR,    0L),          // 00:00
        entry(Prayer.DHUHR,  7200L),        // 02:00
        entry(Prayer.ASR,    14400L),       // 04:00
        entry(Prayer.MAGHRIB, 21600L),      // 06:00
        entry(Prayer.ISHA,   28800L),       // 08:00
    )

    // -------------------------------------------------------------------------
    // Basic correctness
    // -------------------------------------------------------------------------

    @Test
    fun `returns 0 when now is before all prayers`() {
        val now = base.minusSeconds(1)
        assertEquals(0, calculator.findNext(fivePrayers, now))
    }

    @Test
    fun `returns 1 when now is after Fajr but before Dhuhr`() {
        val now = base.plusSeconds(3600) // 01:00 — after Fajr (00:00), before Dhuhr (02:00)
        assertEquals(1, calculator.findNext(fivePrayers, now))
    }

    @Test
    fun `returns 2 when now is after Dhuhr but before Asr`() {
        val now = base.plusSeconds(10800) // 03:00
        assertEquals(2, calculator.findNext(fivePrayers, now))
    }

    @Test
    fun `returns 4 when now is after Maghrib but before Isha`() {
        val now = base.plusSeconds(25200) // 07:00
        assertEquals(4, calculator.findNext(fivePrayers, now))
    }

    @Test
    fun `returns null when now is after all prayers (post-Isha)`() {
        val now = base.plusSeconds(28801) // 1 second after Isha
        assertNull(calculator.findNext(fivePrayers, now))
    }

    @Test
    fun `returns null when now is exactly at Isha time`() {
        // Isha is at base + 28800; now == Isha time → not strictly greater → null
        val now = base.plusSeconds(28800)
        assertNull(calculator.findNext(fivePrayers, now))
    }

    // -------------------------------------------------------------------------
    // Boundary: now exactly at a prayer time
    // -------------------------------------------------------------------------

    @Test
    fun `returns next prayer when now is exactly at Fajr time`() {
        // now == Fajr time → Fajr is not strictly greater → next is Dhuhr (index 1)
        val now = base.plusSeconds(0)
        assertEquals(1, calculator.findNext(fivePrayers, now))
    }

    @Test
    fun `returns next prayer when now is exactly at Dhuhr time`() {
        val now = base.plusSeconds(7200)
        assertEquals(2, calculator.findNext(fivePrayers, now))
    }

    @Test
    fun `returns next prayer when now is exactly at Maghrib time`() {
        val now = base.plusSeconds(21600)
        assertEquals(4, calculator.findNext(fivePrayers, now))
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    fun `returns null for empty list`() {
        assertNull(calculator.findNext(emptyList(), base))
    }

    @Test
    fun `returns 0 for single-element list when now is before it`() {
        val single = listOf(entry(Prayer.FAJR, 3600))
        val now = base
        assertEquals(0, calculator.findNext(single, now))
    }

    @Test
    fun `returns null for single-element list when now is at or after it`() {
        val single = listOf(entry(Prayer.FAJR, 3600))
        val now = base.plusSeconds(3600)
        assertNull(calculator.findNext(single, now))
    }

    @Test
    fun `returns null for single-element list when now is after it`() {
        val single = listOf(entry(Prayer.FAJR, 3600))
        val now = base.plusSeconds(7200)
        assertNull(calculator.findNext(single, now))
    }

    // -------------------------------------------------------------------------
    // isNext field is not modified by the calculator
    // -------------------------------------------------------------------------

    @Test
    fun `does not mutate the input list`() {
        val now = base.plusSeconds(3600)
        val original = fivePrayers.map { it.copy() }
        calculator.findNext(fivePrayers, now)
        // All entries should still have isNext = false (calculator is read-only)
        fivePrayers.forEachIndexed { i, entry ->
            assertEquals(original[i].isNext, entry.isNext,
                "Entry at index $i should not have been mutated")
        }
    }
}
