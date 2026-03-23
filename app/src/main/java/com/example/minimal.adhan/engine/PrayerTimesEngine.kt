package com.example.minimal.adhan.engine

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents

class PrayerTimesEngine {
    fun calculatePrayerTimes(latitude: Double, longitude: Double, date: DateComponents): PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val parameters = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        parameters.madhab = Madhab.SHAFI
        return PrayerTimes(coordinates, date, parameters)
    }
}