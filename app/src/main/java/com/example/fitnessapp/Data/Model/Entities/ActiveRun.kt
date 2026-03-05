package com.example.fitnessapp.Data.Model.Entities

import com.example.fitnessapp.Data.Model.LocationPoints

data class ActiveRun(
    val startTime : Long,
    val elapsedTime : Long,
    val currentDistance : Float,
    val route : List<LocationPoints>,
    val trackingStatus : Boolean = false
)
