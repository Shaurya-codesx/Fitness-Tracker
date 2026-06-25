package com.example.fitnessapp.Domain.UseCases

import com.example.fitnessapp.Data.Model.StatsDataClasses.EnergySplitData
import javax.inject.Inject

class EnergySplitCalculator @Inject constructor(){
    operator fun invoke(rawCalories: List<Float>) : EnergySplitData {
        val validRuns = rawCalories.filter { it > 0f }
        if (validRuns.isEmpty()) return EnergySplitData()

        var lightCount = 0
        var solidCount = 0
        var epicCount = 0

        for (calories in validRuns) {
            when {
                calories < 300f -> lightCount++         // Light Recovery
                calories <= 600f -> solidCount++        // Solid Effort
                else -> epicCount++                     // Epic Burn
            }
        }

        val total = validRuns.size.toFloat()
        return EnergySplitData(
            lightRecoveryPct = (lightCount / total) * 100f,
            solidEffortPct = (solidCount / total) * 100f,
            epicBurnPct = (epicCount / total) * 100f
        )
    }
}