package com.example.fitnessapp.Data.Model

import com.google.firebase.firestore.GeoPoint

data class LocationPoints(
    val coordinates : GeoPoint,
    val timeStamp : Long
)
