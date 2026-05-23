package za.co.amp.prayertimes.domain

/**
 * Geographic coordinates for prayer time calculation.
 *
 * @param latitude Latitude in decimal degrees. Valid range: [-90, 90].
 * @param longitude Longitude in decimal degrees. Valid range: [-180, 180].
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)
