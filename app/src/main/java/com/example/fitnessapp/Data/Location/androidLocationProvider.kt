package com.example.fitnessapp.Data.Location

import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Domain.LocationDataSource
import kotlinx.coroutines.flow.Flow

class androidLocationProvider : LocationDataSource { // this implementation of LocationDataSource tells exactly how the Location Data from android Framework
    // is converted to my LocationPoint model and then collected by repo

    override fun emitLocation(): Flow<LocationPoints> {
        TODO("Not yet implemented")
    }
}