package com.example.fitnessapp.Data.Model

import androidx.annotation.Keep
import com.google.firebase.firestore.GeoPoint

@Keep
data class LocationPoints(
    val coordinates : GeoPoint,
    val timeStamp : Long
)
