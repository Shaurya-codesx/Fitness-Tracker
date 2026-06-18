package com.example.fitnessapp.ui.UiStates

import com.example.fitnessapp.Data.Model.WeeklyDistances

data class StatsData (
    val totalDistance : String ="",
    val totalCalories : String = "",
    val totalSteps : String = "",
    val totalRuns : String = "",
)


sealed class StatsUIState {
    object Loading : StatsUIState()
    data class Success(val data: StatsData) : StatsUIState()
    data class Error(val message: String) : StatsUIState()
}