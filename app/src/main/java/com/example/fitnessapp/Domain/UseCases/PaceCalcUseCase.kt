package com.example.fitnessapp.Domain.UseCases

import androidx.compose.ui.text.intl.Locale
import javax.inject.Inject
import kotlin.math.round
import kotlin.text.format


class PaceCalcUseCase @Inject constructor() {
    operator fun invoke(distanceInMeters: Float, timeInMillis: Long): String {
        if (timeInMillis <= 0L || distanceInMeters <= 0f) {
            return "0:00"
        }

        // 1. Convert units
        val distanceInKm = distanceInMeters / 1000f
        val totalSeconds = timeInMillis / 1000f

        // 2. Calculate total seconds per kilometer
        // (Total Seconds / Total Kilometers)
        val paceInSecondsPerKm = (totalSeconds / distanceInKm).toInt()

        // 3. Extract minutes and remaining seconds
        val minutes = paceInSecondsPerKm / 60
        val seconds = paceInSecondsPerKm % 60

        // 4. Format as M:SS or MM:SS
        // %d for minutes, %02d for seconds (adds a leading zero if less than 10)
        return String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}