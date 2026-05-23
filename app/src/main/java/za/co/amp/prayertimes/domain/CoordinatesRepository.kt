package za.co.amp.prayertimes.domain

import java.time.Instant

/**
 * Persists the last known device coordinates and their acquisition timestamp.
 *
 * Implementations use SharedPreferences with keys defined in the design document.
 */
interface CoordinatesRepository {
    /**
     * Return the most recently cached coordinates, or `null` if coordinates have never been saved.
     */
    fun getCached(): CachedCoordinates?

    /**
     * Persist [coordinates] along with the [timestamp] at which they were obtained.
     */
    fun save(coordinates: Coordinates, timestamp: Instant)

    /**
     * Return `true` if the cached coordinates were obtained within the last [maxAgeHours] hours.
     *
     * Returns `false` if no coordinates have ever been cached.
     *
     * @param maxAgeHours Maximum acceptable age in hours. Default: 24.
     */
    fun isFresh(maxAgeHours: Int = 24): Boolean
}
