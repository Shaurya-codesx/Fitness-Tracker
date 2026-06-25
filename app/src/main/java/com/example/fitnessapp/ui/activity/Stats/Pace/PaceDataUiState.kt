package com.example.fitnessapp.ui.activity.Stats.Pace

import com.example.fitnessapp.Data.Model.StatsDataClasses.PaceSplitData

data class PaceDataUiState(
    val chartData: List<ChartData> = emptyList(),
    val averagePaceDecimal: Float = 0f,
    val paceSplit: PaceSplitData = PaceSplitData() // Add this line
)