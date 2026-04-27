package com.example.fitnessapp.Data.Location

import android.content.Context

import android.os.Looper
import android.util.Log

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Domain.LocationDataSource
import com.example.fitnessapp.Domain.Wrapper.Resource

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class androidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationDataSource { // this implementation of LocationDataSource tells exactly how the Location Data from android Framework
    // is converted to my LocationPoint model and then collected by repo


    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
        .setMinUpdateIntervalMillis(2000L)
//        .setMinUpdateDistanceMeters(3f)
        .build() //An encapsulation of various parameters for requesting location through FusedLocationProviderClient.

    override val locationDataStream: Flow<Resource<LocationPoints>> = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.forEach { location ->
                    if (location.accuracy <= 20f) {
                        val point = LocationPoints(
                            coordinates = GeoPoint(location.latitude, location.longitude),
                            timeStamp = System.currentTimeMillis()
                        )
                        trySend(Resource.Success(point)) // send the point to the Flow subscriber
                        Log.d("lokation", "location point sent : $point")
                    } else {
                        Log.d("lokation", "skipped location point, less accuracy")
                    }
                }
            }
        }
        // here flow itself starts tracking
        // collect Flow → location updates start
        // cancel Flow → location updates stop // no manual start and stop functions needed

        // first we check the settings
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client : SettingsClient = LocationServices.getSettingsClient(context)
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        // if task is success then start location tracking
        task.addOnSuccessListener {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }catch (e : SecurityException) { // this occurs when the app does not has location permission but still tries to access location
                close(e) // close the Flow if permission missing
            }
        }




        // else solve exception and close the thread
        task.addOnFailureListener { exception -> // this is ResolvableApiException, meaning the app has permission to access location
            // but the hardware of the device is turned off for the app
            // handle the fail
            Log.d("lokation", "crashed because location not enabled")
            trySend(Resource.Error(exception)) // we close the flow here
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d("lokation", "stopping location tracking")
        }
    }
}