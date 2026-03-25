package com.example.fitnessapp.ui.activity.RunHistory

import com.example.fitnessapp.Data.Model.LocationPoints

data class RunUiModel(
    val id : Long,
    val startTime : String,
    val endTime : String,
    val duration : String,
    val distanceInMeters : String,
    val avgPace : String,
    val route : List<LocationPoints>
)
