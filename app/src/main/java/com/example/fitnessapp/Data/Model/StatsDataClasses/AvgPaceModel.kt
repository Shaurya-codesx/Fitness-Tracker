package com.example.fitnessapp.Data.Model.StatsDataClasses

data class AvgPaceModel(
    val periodLabel: String,
    val totalDuration: Long,
    val totalDistance: Float
)

data class RawRunModel(
    val durationMillis: Long,
    val distanceMeters: Float
)