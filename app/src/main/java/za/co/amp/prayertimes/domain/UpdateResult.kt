package za.co.amp.prayertimes.domain

/**
 * The result of a widget update cycle performed by WidgetUpdateUseCase.
 */
sealed class UpdateResult {
    /** The update completed successfully with fresh data. */
    data class Success(val state: WidgetState) : UpdateResult()

    /**
     * The update completed but the displayed data is stale (last computation ≥ 24 hours ago).
     * The widget should display the stale-data age label.
     */
    data class StaleData(val state: WidgetState, val ageHours: Int) : UpdateResult()

    /**
     * Location could not be obtained and no cached coordinates are available.
     * The widget should display the missing-location message.
     */
    data class MissingLocation(val message: String) : UpdateResult()

    /**
     * The cached or freshly obtained coordinates are outside valid ranges.
     * The widget should display "Invalid location data" (highest priority error).
     */
    data class InvalidCoordinates(val message: String) : UpdateResult()
}
