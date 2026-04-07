package com.example.fitnessapp.ui.UiStates

import com.example.fitnessapp.Data.Model.LocationPoints

data class TrackingUiState(
    val startTime : String = "",
    val timerValue : String = "",
    val currentDistance : String = "",
    val currentPace : String = "",
    val route : List<LocationPoints> = emptyList()

)
