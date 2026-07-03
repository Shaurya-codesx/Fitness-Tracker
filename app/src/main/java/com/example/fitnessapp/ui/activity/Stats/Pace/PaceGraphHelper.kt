package com.example.fitnessapp.ui.activity.Stats.Pace

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.Data.Model.StatsDataClasses.AvgPaceModel
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale


data class ChartData(
    val displayLabel: String,
    val value: Float
)

@RequiresApi(Build.VERSION_CODES.O)
fun processPaceChartData(
    dbResults: List<AvgPaceModel>,
    startDate: LocalDate,
    endDate: LocalDate,
    filter: FilterRange
): List<ChartData> {
    val dbMap = dbResults.associateBy { it.periodLabel }
    val resultList = mutableListOf<ChartData>()

    when (filter) {
        FilterRange.WEEK -> {
            for (i in 0..6) {
                val currentDate = startDate.plusDays(i.toLong())
                val dbKey = currentDate.toString()
                val label = currentDate.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))

                val rawModel = dbMap[dbKey]
                val paceDecimal = calculatePaceDecimal(rawModel)

                resultList.add(ChartData(label, paceDecimal))
            }
        }
        FilterRange.MONTH -> {
            // Break the month into clean 7-day chunks
            var currentChunkStart = startDate
            var weekNumber = 1

            while (!currentChunkStart.isAfter(endDate)) {
                var chunkTotalDuration = 0L
                var chunkTotalDistance = 0f

                // Sum up the time and distance for these 7 days
                for (i in 0..6) {
                    val day = currentChunkStart.plusDays(i.toLong())

                    if (day.isAfter(endDate)) break

                    val dbKey = day.toString()
                    val dailyData = dbMap[dbKey]

                    chunkTotalDuration += (dailyData?.totalDuration ?: 0L)
                    chunkTotalDistance += (dailyData?.totalDistance ?: 0f)
                }

                // Create a combined model to pass to your existing math function!
                val aggregatedModel = AvgPaceModel(
                    periodLabel = "Wk $weekNumber",
                    totalDuration = chunkTotalDuration,
                    totalDistance = chunkTotalDistance
                )

                // Calculate the true average pace for this 7-day chunk
                val paceDecimal = calculatePaceDecimal(aggregatedModel)

                resultList.add(ChartData("Week $weekNumber", paceDecimal))

                // Jump forward 7 days for the next week chunk
                currentChunkStart = currentChunkStart.plusDays(7)
                weekNumber++
            }
        }
        FilterRange.YEAR -> {
            for (i in 1..12) {
                val monthString = String.format("%02d", i)
                val dbKey = "${startDate.year}-$monthString"

                val monthDate = LocalDate.of(startDate.year, i, 1)
                val label = monthDate.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))

                val rawModel = dbMap[dbKey]
                val paceDecimal = calculatePaceDecimal(rawModel)

                resultList.add(ChartData(label, paceDecimal))
            }
        }
    }
    return resultList
}

// Extracted the math formula to keep the loop clean
private fun calculatePaceDecimal(rawModel: AvgPaceModel?): Float {
    if (rawModel == null || rawModel.totalDistance <= 0f) return 0f

    // 1. Convert duration to exact minutes (e.g., 25.5 mins)
    val durationMinutes = rawModel.totalDuration / 60000f

    // 2. Convert distance to Kilometers (REMOVE '/ 1000f' if DB already stores KM)
    val distanceKm = rawModel.totalDistance / 1000f

    // 3. Pace = Time / Distance (e.g., 25.5 / 5.0 = 5.1 min/km)
    return durationMinutes / distanceKm
}