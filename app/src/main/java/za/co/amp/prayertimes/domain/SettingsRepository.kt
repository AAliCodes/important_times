package za.co.amp.prayertimes.domain

/**
 * Persists user preferences for the prayer times widget.
 *
 * Implementations use SharedPreferences with keys defined in the design document.
 *
 * Defaults:
 * - Calculation method: [CalculationMethod.MUSLIM_WORLD_LEAGUE]
 * - Show sunrise: `false`
 */
interface SettingsRepository {
    /**
     * Return the currently selected calculation method.
     * Returns [CalculationMethod.MUSLIM_WORLD_LEAGUE] if the user has never configured a method.
     */
    fun getCalculationMethod(): CalculationMethod

    /**
     * Persist [method] as the selected calculation method.
     */
    fun setCalculationMethod(method: CalculationMethod)

    /**
     * Return `true` if the user has enabled the optional Sunrise entry on the widget.
     * Returns `false` by default.
     */
    fun isShowSunrise(): Boolean

    /**
     * Persist the sunrise display preference.
     */
    fun setShowSunrise(show: Boolean)
}
