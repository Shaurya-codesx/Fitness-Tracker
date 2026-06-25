package com.example.fitnessapp.Domain.UseCases

import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceSplitData
import javax.inject.Inject

class DistanceSplitCalculator @Inject constructor() {
    operator fun invoke (distances: List<Float>): DistanceSplitData {
        if (distances.isEmpty()) return DistanceSplitData()

        val totalRuns = distances.size.toFloat()

        val lessThan5 = distances.count { it < 5000f }
        val fiveTo10 = distances.count { it in 5000f..10000f }
        val moreThan10 = distances.count { it > 10000f }

        return DistanceSplitData(
            lessThan5k = (lessThan5 / totalRuns) * 100f,
            fiveTo10k = (fiveTo10 / totalRuns) * 100f,
            moreThan10k = (moreThan10 / totalRuns) * 100f
        )
    }
}