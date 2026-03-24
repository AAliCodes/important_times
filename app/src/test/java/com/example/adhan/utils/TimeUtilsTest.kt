package com.example.minimal.adhan.utils

import com.example.minimal.adhan.utils.formatToTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class TimeUtilsTest {

    @Test
    fun `formatToTime returns correctly formatted 12h string`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
        }
        
        val formattedTime = calendar.time.formatToTime()
        
        assertEquals("02:30 pm", formattedTime)
    }
}