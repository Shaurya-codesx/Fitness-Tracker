package com.example.fitnessapp.Domain.UseCases

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ConvertTimeUseCase @Inject constructor() {
    operator fun invoke (timeMillis : Long) : String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun timerFormat(milliseconds: Long): String {
        val seconds = milliseconds/1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun formatDurationShort(milliseconds: Long): String {
        val totalSeconds = milliseconds /1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("EEEE, MMM, d", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

}