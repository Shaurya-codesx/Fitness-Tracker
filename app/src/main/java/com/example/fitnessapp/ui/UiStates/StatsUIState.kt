package com.example.fitnessapp.ui.UiStates

data class StatsData (
    val totalDistance : String ="",
    val totalTime : String = "",
    val totalAvgPace : String = "",
    val totalRuns : String = ""
)


sealed class StatsUIState {
    object Loading : StatsUIState()
    data class Success(val data: StatsData) : StatsUIState()
    data class Error(val message: String) : StatsUIState()
}