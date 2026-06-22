package com.example.fitnessapp.ui.activity.Stats.Steps

data class StepsDataUiState (
    val chartData: List<ChartData> = emptyList(),
    val dailyAverage: Int = 0,
    val totalSteps: Int = 0
)