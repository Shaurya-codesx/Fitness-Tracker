package com.example.fitnessapp.ui.UiStates

import com.example.fitnessapp.Data.Model.WeeklyDistances

data class StatsData (
    val totalDistance : String ="",
    val totalTime : String = "",
    val totalAvgPace : String = "",
    val totalRuns : String = "",
    val weeklyDistanceData : List<WeeklyDistances> = emptyList()
)


sealed class StatsUIState {
    object Loading : StatsUIState()
    data class Success(val data: StatsData) : StatsUIState()
    data class Error(val message: String) : StatsUIState()
}