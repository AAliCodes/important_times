package za.co.amp.prayertimes.domain

/**
 * Obtains the device's current geographic coordinates.
 *
 * Implementations wrap [com.google.android.gms.location.FusedLocationProviderClient].
 */
interface LocationProvider {
    /**
     * Request the device's current location.
     *
     * This is a suspending function. It will wait up to [timeoutMs] milliseconds for a fix.
     *
     * @param timeoutMs Maximum time to wait for a location fix, in milliseconds. Default: 30 000.
     * @return A [LocationResult] describing the outcome.
     */
    suspend fun getLocation(timeoutMs: Long = 30_000L): LocationResult
}
