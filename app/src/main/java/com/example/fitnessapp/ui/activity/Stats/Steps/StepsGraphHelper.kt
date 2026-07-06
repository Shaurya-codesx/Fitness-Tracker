package com.example.fitnessapp.ui.activity.Stats.Steps

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.Data.Model.StatsDataClasses.StepsModel
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

data class ChartData(
    val displayLabel: String, // "Mon", "Week 1", "Jan"
    val value: Int
)


@RequiresApi(Build.VERSION_CODES.O)
fun processChartData(
    dbResults: List<StepsModel>,
    startDate: LocalDate,
    endDate: LocalDate,
    filter: FilterRange
): List<ChartData> {

    val dbMap = dbResults.associateBy { it.periodLabel }
    val resultList = mutableListOf<ChartData>()

    when (filter) {
        FilterRange.WEEK -> {
            // Generate 7 days (Mon-Sun)
            for (i in 0..6) {
                val currentDate = startDate.plusDays(i.toLong())
                val dbKey = currentDate.toString() // "YYYY-MM-DD"

                // Format for UI: "Mon", "Tue"
                val label = currentDate.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))

                resultList.add(ChartData(label, dbMap[dbKey]?.stepCount ?: 0))
            }
        }

        FilterRange.MONTH -> {
            // Generate weeks for the specific month
            var currentChunkStart = startDate
            var weekNumber = 1

            while (!currentChunkStart.isAfter(endDate)) {
                var chunkSum = 0

                // Sum up the 7 days for this specific week block
                for (i in 0..6) {
                    val day = currentChunkStart.plusDays(i.toLong())

                    // Stop if we accidentally bleed into August
                    if (day.isAfter(endDate)) break

                    val dbKey = day.toString() // e.g., "2026-07-03"
                    chunkSum += (dbMap[dbKey]?.stepCount ?: 0)
                }

                resultList.add(ChartData("Week $weekNumber", chunkSum))

                // Jump forward 7 days for the next week chunk
                currentChunkStart = currentChunkStart.plusDays(7)
                weekNumber++
            }
        }

        FilterRange.YEAR -> {
            // Generate 12 months (Jan-Dec)
            for (i in 1..12) {
                // DB Key format: "YYYY-MM"
                val monthString = String.format("%02d", i)
                val dbKey = "${startDate.year}-$monthString"

                // UI Label: "Jan", "Feb"
                val monthDate = LocalDate.of(startDate.year, i, 1)
                val label = monthDate.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))

                resultList.add(ChartData(label, dbMap[dbKey]?.stepCount ?: 0))
            }
        }
    }

    return resultList
}