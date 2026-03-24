package com.example.minimal.adhan.ui

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.layout.Alignment
import com.batoulapps.adhan.data.DateComponents
import com.example.minimal.adhan.data.DataStoreUserRepository
import com.example.minimal.adhan.engine.PrayerTimesEngine
import com.example.minimal.adhan.utils.formatToTime
import kotlinx.coroutines.flow.first
import java.util.Date

class PrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userRepository = DataStoreUserRepository(context)
        val engine = PrayerTimesEngine()
        val coordinates = userRepository.getLocation().first()

        val prayerTimes = if (coordinates != null) {
            val times = engine.calculatePrayerTimes(
                coordinates.latitude, coordinates.longitude, DateComponents.from(Date())
            )
            listOf(
                "Fajr" to times.fajr.formatToTime(),
                "Dhuhr" to times.dhuhr.formatToTime(),
                "Asr" to times.asr.formatToTime(),
                "Maghrib" to times.maghrib.formatToTime(),
                "Isha" to times.isha.formatToTime()
            )
        } else {
            emptyList()
        }

        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                if (prayerTimes.isEmpty()) {
                    Text(text = "Open app to set location", style = TextStyle(fontSize = 12.sp))
                } else {
                    prayerTimes.forEach { (name, time) ->
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.Horizontal.Start,
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = name,
                                modifier = GlanceModifier.defaultWeight(),
                                style = TextStyle(fontSize = 14.sp)
                            )
                            Text(
                                text = time,
                                style = TextStyle(fontSize = 14.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}