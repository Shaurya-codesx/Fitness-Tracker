package com.example.fitnessapp.Domain.UseCases

import javax.inject.Inject

class CalculateCaloriesUseCase @Inject constructor() {

    operator fun invoke(distanceMeters: Float, durationMillis: Long, weightKG: Float): Float {
        // 1. Guard clauses to prevent division by zero or invalid data
        if (durationMillis <= 0L || distanceMeters <= 0f || weightKG <= 0f) return 0f

        // 2. Convert time to hours for the MET formula
        val durationHours = durationMillis / 3600000f // 1000ms * 60s * 60m

        // 3. Calculate speed in km/h to determine intensity
        val distanceKm = distanceMeters / 1000f
        val speedKmh = distanceKm / durationHours


        // 4. Determine the MET value based on speed
        // (Values derived from the Compendium of Physical Activities)
        val met = when {
            speedKmh < 1.0f -> 1.5f   // Standing/very slow stroll
            speedKmh < 3.2f -> 2.0f   // Slow walk
            speedKmh < 4.8f -> 3.3f   // Brisk walk
            speedKmh < 6.4f -> 4.3f   // Fast walk
            speedKmh < 8.0f -> 7.0f   // Jogging
            speedKmh < 9.7f -> 8.3f   // Running (~6 mph)
            speedKmh < 11.3f -> 9.8f  // Running (~7 mph)
            speedKmh < 12.9f -> 10.5f // Running (~8 mph)
            speedKmh < 14.5f -> 11.8f // Running (~9 mph)
            speedKmh < 16.1f -> 12.8f // Running (~10 mph)
            else -> 16.0f             // Very fast running/sprinting
        }

        // 5. Apply the MET formula
        return met * weightKG * durationHours
    }
}