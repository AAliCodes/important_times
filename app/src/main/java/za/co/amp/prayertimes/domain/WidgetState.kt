package za.co.amp.prayertimes.domain

import java.time.Instant

/**
 * The complete state object passed to the Glance composable via GlanceStateDefinition.
 *
 * @param prayers The five daily prayer entries (Fajr, Dhuhr, Asr, Maghrib, Isha).
 * @param sunrise Optional sunrise entry, shown only when the user has enabled it in settings.
 * @param lastUpdated The UTC instant of the last successful prayer time computation.
 * @param isLocationCached True when the displayed times are based on cached (not fresh) coordinates.
 * @param errorMessage Non-null when an error should be displayed in place of or alongside prayer times.
 * @param staleDataAgeHours Non-null when the last computation is 24 or more hours old; value is
 *   floor((now - lastUpdated).toHours()).
 */
data class WidgetState(
    val prayers: List<PrayerEntry>,
    val sunrise: PrayerEntry?,
    val lastUpdated: Instant,
    val isLocationCached: Boolean,
    val errorMessage: String?,
    val staleDataAgeHours: Int?,
)
