package za.co.amp.prayertimes.domain

/**
 * Schedules exact AlarmManager alarms that fire at each prayer time to trigger a widget refresh.
 *
 * Implementations use [android.app.AlarmManager.setExactAndAllowWhileIdle] so that alarms fire
 * even when the device is in Doze mode. On API 31+ the implementation must check
 * [android.app.AlarmManager.canScheduleExactAlarms] and fall back to
 * [android.app.AlarmManager.setAndAllowWhileIdle] if exact alarms are not permitted.
 */
interface PrayerAlarmScheduler {
    /**
     * Schedule an exact alarm for each entry in [prayers].
     *
     * Each alarm broadcasts [za.co.amp.prayertimes.ui.widget.WidgetReceiver.ACTION_PRAYER_ALARM]
     * to [za.co.amp.prayertimes.ui.widget.WidgetReceiver].
     *
     * @param prayers The list of prayer entries for which to schedule alarms.
     */
    fun scheduleNext(prayers: List<PrayerEntry>)

    /**
     * Cancel all pending prayer alarms previously scheduled by [scheduleNext].
     */
    fun cancelAll()
}
