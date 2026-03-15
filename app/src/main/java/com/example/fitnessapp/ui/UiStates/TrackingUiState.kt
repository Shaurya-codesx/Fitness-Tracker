package com.example.fitnessapp.ui.UiStates

import com.example.fitnessapp.Data.Model.LocationPoints

data class TrackingUiState(
    val elapsedTime : Long,
    val currentDistance : Float,
    val currentPace : Float,
    val route : List<LocationPoints>
)
