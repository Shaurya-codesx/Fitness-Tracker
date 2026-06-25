package com.example.fitnessapp.ui.activity.Stats.Pace

data class PaceDataUiState (
    val chartData: List<ChartData> = emptyList(),
    val averagePaceDecimal: Float = 0f
)