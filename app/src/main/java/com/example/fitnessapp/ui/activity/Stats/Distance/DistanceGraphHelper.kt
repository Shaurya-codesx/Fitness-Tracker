package com.example.fitnessapp.ui.activity.Stats.Distance

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceModel
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale



data class ChartData(
    val displayLabel: String,
    val value: Float
)

@RequiresApi(Build.VERSION_CODES.O)
fun processDistanceChartData(
    dbResults: List<DistanceModel>,
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

                // Directly fetch the float or default to 0f
                val distance = dbMap[dbKey]?.totalDistance ?: 0f
                resultList.add(ChartData(label, distance/1000))
            }
        }
        FilterRange.MONTH -> {
            // We break the month into clean 7-day chunks (Week 1 = 1st-7th, Week 2 = 8th-14th, etc.)
            // This bypasses the SQLite vs. Kotlin week-number mismatch!
            var currentChunkStart = startDate
            var weekNumber = 1

            while (!currentChunkStart.isAfter(endDate)) {
                var chunkSum = 0f // Using Float for distance

                // Sum up the 7 days for this specific week block
                for (i in 0..6) {
                    val day = currentChunkStart.plusDays(i.toLong())

                    // Stop if we accidentally bleed into the next month
                    if (day.isAfter(endDate)) break

                    val dbKey = day.toString() // Matches the DAO '%Y-%m-%d' format exactly

                    // Grab the distance for this day, or 0f if they didn't run
                    chunkSum += (dbMap[dbKey]?.totalDistance ?: 0f)
                }

                // Add the sum to the chart, keeping your /1000 conversion to km!
                resultList.add(ChartData("Week $weekNumber", chunkSum / 1000f))

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

                val distance = dbMap[dbKey]?.totalDistance ?: 0f
                resultList.add(ChartData(label, distance/1000))
            }
        }
    }

    return resultList
}