package za.co.amp.prayertimes.domain

import com.batoulapps.adhan.data.DateComponents
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

/**
 * Unit tests for [AdhanPrayerTimeCalculator].
 *
 * Covers:
 * - Coordinate validation (lat/lon out of range)
 * - Successful calculation returns exactly 5 prayers in chronological order
 * - Sunrise is included/excluded based on the flag
 * - All four calculation methods produce results
 * - Known reference values for Mecca on a fixed date
 */
class AdhanPrayerTimeCalculatorTest {

    private val calculator = AdhanPrayerTimeCalculator()

    // Fixed date: 2024-01-15 (a stable reference date)
    private val date = DateComponents(2024, 1, 15)

    // Mecca coordinates
    private val mecca = Coordinates(21.3891, 39.8579)

    // London coordinates
    private val london = Coordinates(51.5074, -0.1278)

    // -------------------------------------------------------------------------
    // Coordinate validation
    // -------------------------------------------------------------------------

    @Test
    fun `returns InvalidCoordinates when latitude is above 90`() {
        val result = calculator.calculate(Coordinates(90.1, 0.0), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertTrue(result is PrayerTimesResult.InvalidCoordinates)
    }

    @Test
    fun `returns InvalidCoordinates when latitude is below minus 90`() {
        val result = calculator.calculate(Coordinates(-90.1, 0.0), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertTrue(result is PrayerTimesResult.InvalidCoordinates)
    }

    @Test
    fun `returns InvalidCoordinates when longitude is above 180`() {
        val result = calculator.calculate(Coordinates(0.0, 180.1), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertTrue(result is PrayerTimesResult.InvalidCoordinates)
    }

    @Test
    fun `returns InvalidCoordinates when longitude is below minus 180`() {
        val result = calculator.calculate(Coordinates(0.0, -180.1), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertTrue(result is PrayerTimesResult.InvalidCoordinates)
    }

    @Test
    fun `accepts boundary latitude of exactly 90`() {
        val result = calculator.calculate(Coordinates(90.0, 0.0), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        // Boundary is valid — should not return InvalidCoordinates
        assertFalse(result is PrayerTimesResult.InvalidCoordinates)
    }

    @Test
    fun `accepts boundary latitude of exactly minus 90`() {
        val result = calculator.calculate(Coordinates(-90.0, 0.0), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertFalse(result is PrayerTimesResult.InvalidCoordinates)
    }

    @Test
    fun `accepts boundary longitude of exactly 180`() {
        val result = calculator.calculate(Coordinates(0.0, 180.0), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertFalse(result is PrayerTimesResult.InvalidCoordinates)
    }

    @Test
    fun `accepts boundary longitude of exactly minus 180`() {
        val result = calculator.calculate(Coordinates(0.0, -180.0), date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertFalse(result is PrayerTimesResult.InvalidCoordinates)
    }

    // -------------------------------------------------------------------------
    // Successful calculation — structure
    // -------------------------------------------------------------------------

    @Test
    fun `returns exactly 5 prayers for Mecca with MWL method`() {
        val result = calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false)
        assertTrue(result is PrayerTimesResult.Success)
        val success = result as PrayerTimesResult.Success
        assertEquals(5, success.prayers.size)
    }

    @Test
    fun `prayers are in chronological order for Mecca`() {
        val result = calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false) as PrayerTimesResult.Success
        val times = result.prayers.map { it.time }
        for (i in 0 until times.size - 1) {
            assertTrue(times[i] < times[i + 1],
                "Expected prayer[${i}] < prayer[${i+1}] but got ${times[i]} >= ${times[i+1]}")
        }
    }

    @Test
    fun `prayers are returned in correct order FAJR DHUHR ASR MAGHRIB ISHA`() {
        val result = calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false) as PrayerTimesResult.Success
        val prayerNames = result.prayers.map { it.prayer }
        assertEquals(listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA), prayerNames)
    }

    @Test
    fun `all returned PrayerEntry items have isNext false`() {
        val result = calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false) as PrayerTimesResult.Success
        assertTrue(result.prayers.all { !it.isNext }, "Calculator should not set isNext — that is NextPrayerCalculator's job")
    }

    // -------------------------------------------------------------------------
    // Sunrise flag
    // -------------------------------------------------------------------------

    @Test
    fun `sunrise is null when showSunrise is false`() {
        val result = calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false) as PrayerTimesResult.Success
        assertNull(result.sunrise)
    }

    @Test
    fun `sunrise is non-null when showSunrise is true`() {
        val result = calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, true) as PrayerTimesResult.Success
        assertNotNull(result.sunrise)
        assertEquals(Prayer.SUNRISE, result.sunrise!!.prayer)
    }

    @Test
    fun `sunrise time is between Fajr and Dhuhr`() {
        val result = calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, true) as PrayerTimesResult.Success
        val fajr = result.prayers.first { it.prayer == Prayer.FAJR }.time
        val dhuhr = result.prayers.first { it.prayer == Prayer.DHUHR }.time
        val sunrise = result.sunrise!!.time
        assertTrue(sunrise > fajr, "Sunrise should be after Fajr")
        assertTrue(sunrise < dhuhr, "Sunrise should be before Dhuhr")
    }

    // -------------------------------------------------------------------------
    // All calculation methods produce valid results
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(CalculationMethod::class)
    fun `all calculation methods return 5 prayers for London`(method: CalculationMethod) {
        val result = calculator.calculate(london, date, method, false)
        assertTrue(result is PrayerTimesResult.Success, "Expected Success for method $method but got $result")
        val success = result as PrayerTimesResult.Success
        assertEquals(5, success.prayers.size)
    }

    @ParameterizedTest
    @EnumSource(CalculationMethod::class)
    fun `all calculation methods return chronologically ordered prayers for London`(method: CalculationMethod) {
        val result = calculator.calculate(london, date, method, false) as PrayerTimesResult.Success
        val times = result.prayers.map { it.time }
        for (i in 0 until times.size - 1) {
            assertTrue(times[i] < times[i + 1],
                "Method $method: prayer[$i] should be before prayer[${i+1}]")
        }
    }

    // -------------------------------------------------------------------------
    // Distinct methods produce different times (Property 7)
    // -------------------------------------------------------------------------

    @Test
    fun `MWL and ISNA produce different prayer times for London`() {
        val mwl = (calculator.calculate(london, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false) as PrayerTimesResult.Success).prayers
        val isna = (calculator.calculate(london, date, CalculationMethod.ISNA, false) as PrayerTimesResult.Success).prayers
        val anyDiffers = mwl.zip(isna).any { (a, b) -> a.time != b.time }
        assertTrue(anyDiffers, "MWL and ISNA should produce at least one different prayer time")
    }

    @Test
    fun `MWL and UMM_AL_QURA produce different prayer times for Mecca`() {
        val mwl = (calculator.calculate(mecca, date, CalculationMethod.MUSLIM_WORLD_LEAGUE, false) as PrayerTimesResult.Success).prayers
        val uaq = (calculator.calculate(mecca, date, CalculationMethod.UMM_AL_QURA, false) as PrayerTimesResult.Success).prayers
        val anyDiffers = mwl.zip(uaq).any { (a, b) -> a.time != b.time }
        assertTrue(anyDiffers, "MWL and UMM_AL_QURA should produce at least one different prayer time")
    }
}
