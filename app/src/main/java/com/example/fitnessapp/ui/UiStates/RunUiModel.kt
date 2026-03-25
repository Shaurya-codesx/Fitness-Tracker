package com.example.fitnessapp.ui.UiStates

data class RunUiModel(
    val id : Long,
    val startTime : String,
    val endTime : String,
    val duration : String,
    val distanceInMeters : String,
    val avgPace : String,
)