package com.example.fitnessapp.ui.activity.RunHistory.RunDetails

import com.example.fitnessapp.Data.Model.LocationPoints

data class RunDetailsUiState(
    val runId: Long,
    val startTime : String = "",
    val endTime : String = "",
    val date : String = "",
    val duration : String = "",
    val distance : String = "",
    val avgPace : String = "",
    val stepsTaken : String = "",
    val caloriesBurned : String = "",
    val routeList : List<LocationPoints> = emptyList()
)
