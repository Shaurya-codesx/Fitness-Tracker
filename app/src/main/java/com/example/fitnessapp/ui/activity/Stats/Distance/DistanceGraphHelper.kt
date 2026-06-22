package com.example.fitnessapp.ui.activity.Stats.Distance

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceModel
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
            val weekFields = WeekFields.of(Locale.getDefault())
            var currentWeekStart = startDate
            var weekNumber = 1

            while (!currentWeekStart.isAfter(endDate)) {
                val yearFormatter = DateTimeFormatter.ofPattern("YYYY")
                val weekOfYear = currentWeekStart.get(weekFields.weekOfYear())
                val dbKey = "${currentWeekStart.format(yearFormatter)}-${String.format("%02d", weekOfYear)}"

                val distance = dbMap[dbKey]?.totalDistance ?: 0f
                resultList.add(ChartData("Week $weekNumber", distance/1000))

                currentWeekStart = currentWeekStart.plusWeeks(1)
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