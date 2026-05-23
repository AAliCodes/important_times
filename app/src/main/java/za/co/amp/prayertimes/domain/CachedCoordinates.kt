package za.co.amp.prayertimes.domain

import java.time.Instant

/**
 * Coordinates that have been persisted to local storage along with the time they were obtained.
 *
 * @param coordinates The cached geographic coordinates.
 * @param timestamp The UTC instant at which these coordinates were last obtained from the device.
 */
data class CachedCoordinates(
    val coordinates: Coordinates,
    val timestamp: Instant,
)
