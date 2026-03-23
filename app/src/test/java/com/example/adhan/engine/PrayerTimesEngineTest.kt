package com.example.adhan.engine

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.example.minimal.adhan.engine.PrayerTimesEngine
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimesEngineTest {

    @Test
    fun `calculatePrayerTimes returns valid times for known date and location`() {
        val engine = PrayerTimesEngine()
        val coordinates = Coordinates(21.4225, 39.8262) // Mecca
        val fixedDate = DateComponents(2026, 3, 22)

        val prayerTimes = engine.calculatePrayerTimes(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
            date = fixedDate
        )

        assertNotNull("Fajr time should not be null", prayerTimes.fajr)
        assertNotNull("Dhuhr time should not be null", prayerTimes.dhuhr)
        assertTrue(
            "Fajr should be before Dhuhr",
            prayerTimes.fajr.time < prayerTimes.dhuhr.time
        )
    }
}