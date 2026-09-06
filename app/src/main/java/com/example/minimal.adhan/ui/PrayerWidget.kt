package com.example.minimal.adhan.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.batoulapps.adhan.data.DateComponents
import com.example.minimal.adhan.MainActivity
import com.example.minimal.adhan.data.DataStoreUserRepository
import com.example.minimal.adhan.engine.PrayerTimesEngine
import com.example.minimal.adhan.utils.formatToTime
import kotlinx.coroutines.flow.first
import java.util.Date

class PrayerWidget : GlanceAppWidget() {

    // Define responsive sizes for the widget
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(100.dp, 100.dp), DpSize(200.dp, 100.dp), DpSize(250.dp, 200.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userRepository = DataStoreUserRepository(context)
        val engine = PrayerTimesEngine()
        
        // Fetch location and user's preferred Madhab from DataStore
        val coordinates = userRepository.getLocation().first()
        val madhab = userRepository.getMadhab().first()

        val now = Date()
        val prayerData = if (coordinates != null) {
            val times = engine.calculatePrayerTimes(
                coordinates.latitude, coordinates.longitude, DateComponents.from(now), madhab
            )
            val list = listOf(
                "Fajr" to times.fajr,
                "Dhuhr" to times.dhuhr,
                "Asr" to times.asr,
                "Maghrib" to times.maghrib,
                "Isha" to times.isha
            )
            // Find the index of the next prayer
            val nextIdx = list.indexOfFirst { it.second.after(now) }.let { if (it == -1) 0 else it }
            
            list.mapIndexed { index, pair ->
                Triple(pair.first, pair.second.formatToTime(), index == nextIdx)
            }
        } else {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                WidgetContent(prayerData)
            }
        }
    }

    @Composable
    private fun WidgetContent(prayerData: List<Triple<String, String, Boolean>>) {
        val size = LocalSize.current
        val isSmall = size.width < 150.dp

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            if (prayerData.isEmpty()) {
                Text(
                    text = "Open app to set location",
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp)
                )
            } else if (isSmall) {
                // Compact Layout: Only show the next prayer time
                val next = prayerData.find { it.third } ?: prayerData.first()
                Text(
                    text = next.first,
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = next.second,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            } else {
                // Flexible Layout: List all prayer times, highlighting the next one
                prayerData.forEach { (name, time, isNext) ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = name,
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(
                                color = if (isNext) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        Text(
                            text = time,
                            style = TextStyle(
                                color = if (isNext) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}
