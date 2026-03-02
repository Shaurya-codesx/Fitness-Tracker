package com.example.fitnessapp.Domain

import com.example.fitnessapp.Data.Model.LocationPoints
import javax.inject.Inject
import kotlin.math.*

class CalcDistanceUseCase @Inject constructor() {
    operator fun invoke(lastPoint : LocationPoints, currentPoint : LocationPoints) : Double {
        val earthRadiusMeters = 6_371_000.0

        val dLat = Math.toRadians(currentPoint.coordinates.latitude - lastPoint.coordinates.latitude)
        val dLon = Math.toRadians(currentPoint.coordinates.longitude - lastPoint.coordinates.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lastPoint.coordinates.latitude)) * cos(Math.toRadians(currentPoint.coordinates.latitude)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusMeters * c
    }
}