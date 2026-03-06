package com.example.fitnessapp.Data.Location

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import androidx.navigationevent.NavigationEventDispatcher
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Domain.LocationDataSource
import kotlinx.coroutines.flow.Flow

class androidLocationProvider : LocationDataSource { // this implementation of LocationDataSource tells exactly how the Location Data from android Framework
    // is converted to my LocationPoint model and then collected by repo

    override val locationDataStream: Flow<LocationPoints>
        get() = TODO("Not yet implemented")

    override fun startLocationTracking() {
        TODO("Not yet implemented")

    }

    override fun stopLocationTracking() {
        TODO("Not yet implemented")
    }


    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .setMinUpdateIntervalMillis(3000L)
        .setMinUpdateDistanceMeters(3f)
        .build()
    //An encapsulation of various parameters for requesting location through FusedLocationProviderClient.


}