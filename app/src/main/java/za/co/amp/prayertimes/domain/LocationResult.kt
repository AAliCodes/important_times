package za.co.amp.prayertimes.domain

import java.time.Instant

/**
 * The result of a location request performed by [LocationProvider].
 */
sealed class LocationResult {
    /**
     * A location fix was obtained successfully.
     *
     * @param coordinates The device's current geographic coordinates.
     * @param timestamp The UTC instant at which the fix was obtained.
     */
    data class Success(
        val coordinates: Coordinates,
        val timestamp: Instant,
    ) : LocationResult()

    /** The user has not granted ACCESS_COARSE_LOCATION permission. */
    object PermissionDenied : LocationResult()

    /** No location fix was obtained within the configured timeout. */
    object Timeout : LocationResult()

    /** The location provider returned a null result (e.g., no last-known location). */
    object Unavailable : LocationResult()
}
