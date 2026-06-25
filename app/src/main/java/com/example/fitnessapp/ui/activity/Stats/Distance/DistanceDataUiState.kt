package com.example.fitnessapp.ui.activity.Stats.Distance

import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceSplitData

data class DistanceDataUiState (
    val chartData: List<ChartData> = emptyList(),
    val dailyAverage: Float = 0f,
    val totalDistance: Float = 0f,
    val distanceSplit: DistanceSplitData = DistanceSplitData()
)