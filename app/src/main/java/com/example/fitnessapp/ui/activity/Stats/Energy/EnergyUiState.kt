package com.example.fitnessapp.ui.activity.Stats.Energy

import com.example.fitnessapp.Data.Model.StatsDataClasses.EnergySplitData

data class EnergyUiState(
    val chartData: List<ChartData> = emptyList(),
    val dailyAverage: Float = 0f,
    val totalCalories: Float = 0f,
    val energySplit: EnergySplitData = EnergySplitData()
)