package za.co.amp.prayertimes.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * AppWidget receiver that handles update broadcasts and prayer alarm events.
 * Full implementation is in task 9.3.
 */
class WidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = PrayerTimesWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // TODO: task 9.3 — handle ACTION_PRAYER_ALARM and manual refresh
    }
}
