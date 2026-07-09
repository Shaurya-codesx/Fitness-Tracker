package com.example.fitnessapp.Data.Model.Entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitnessapp.Data.Model.LocationPoints

@Keep
@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,
    val startTime : Long,
    val endTime : Long,
    val distanceInMeters : Float,
    val stepsTaken : Int,
    val caloriesBurned : Float = 0f,
    val route : List<LocationPoints>, // List of points, each element of the list has a lat, long, and time which creates the route
    val isSynced : Boolean
)