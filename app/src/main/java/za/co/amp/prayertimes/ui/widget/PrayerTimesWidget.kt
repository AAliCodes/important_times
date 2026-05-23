package za.co.amp.prayertimes.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

/**
 * Glance App Widget that displays Islamic prayer times on the home screen.
 * Full implementation is in task 9.1.
 */
class PrayerTimesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // TODO: task 9.1 — render full prayer times layout
            Text("Prayer Times")
        }
    }
}
