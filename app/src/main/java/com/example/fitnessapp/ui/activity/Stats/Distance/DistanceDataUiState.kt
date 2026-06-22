package com.example.fitnessapp.ui.activity.Stats.Distance

data class DistanceDataUiState (
    val chartData: List<ChartData> = emptyList(),
    val dailyAverage: Float = 0f,
    val totalDistance: Float = 0f
)