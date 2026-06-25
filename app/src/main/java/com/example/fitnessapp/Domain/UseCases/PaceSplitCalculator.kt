package com.example.fitnessapp.Domain.UseCases

import com.example.fitnessapp.Data.Model.StatsDataClasses.PaceSplitData
import com.example.fitnessapp.Data.Model.StatsDataClasses.RawRunModel
import javax.inject.Inject

class PaceSplitCalculator @Inject constructor() {
    operator fun invoke (rawRuns: List<RawRunModel>) : PaceSplitData {
        val validRuns = rawRuns.filter { it.distanceMeters > 0f }
        if (validRuns.isEmpty()) return PaceSplitData()

        var walkingCount = 0
        var joggingCount = 0
        var runningCount = 0

        for (run in validRuns) {
            val durationMins = run.durationMillis / 60000f
            val distanceKm = run.distanceMeters / 1000f // Remove /1000f if your DB stores KM
            val paceDecimal = durationMins / distanceKm

            when {
                paceDecimal > 8.0f -> walkingCount++      // Slower than 8:00/km
                paceDecimal >= 6.0f -> joggingCount++     // Between 6:00/km and 8:00/km
                else -> runningCount++                    // Faster than 6:00/km
            }
        }

        val total = validRuns.size.toFloat()
        return PaceSplitData(
            walkingPct = (walkingCount / total) * 100f,
            joggingPct = (joggingCount / total) * 100f,
            runningPct = (runningCount / total) * 100f
        )
    }
}