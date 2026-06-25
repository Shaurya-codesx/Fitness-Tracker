package com.example.fitnessapp.Domain.UseCases

import com.example.fitnessapp.ui.activity.Stats.Pace.ChartData

import kotlin.math.roundToInt

object PaceFormatterUseCase{

    fun formatDecimalPaceToString(decimalPace: Float): String {
        if (decimalPace <= 0f) return "0:00"

        val minutes = decimalPace.toInt()
        val seconds = ((decimalPace - minutes) * 60).roundToInt()

        // Handle edge case where seconds round up to 60
        return if (seconds == 60) {
            String.format("%d:00", minutes + 1)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    fun preparePaceChartSeries(chartData: List<ChartData>): List<Float?> {
        return chartData.map { chartItem ->
            if (chartItem.value <= 0f) {
                null // Skip drawing a point for days with no runs
            } else {
                -chartItem.value // Invert for the UI
            }
        }
    }
}