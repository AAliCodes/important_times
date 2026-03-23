package com.example.minimal.adhan.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Date.formatToTime(): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(this)
}