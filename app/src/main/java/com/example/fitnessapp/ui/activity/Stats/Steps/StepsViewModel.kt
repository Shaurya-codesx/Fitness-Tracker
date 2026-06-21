package com.example.fitnessapp.ui.activity.Stats.Steps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class StepsViewModel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel(){

    /**
     * This function will be called by your Compose Pager for every page.
     * offset = 0 is current week/month/year.
     * offset = -1 is the previous week/month/year, etc.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getStepsDataForPage(filter: FilterRange, offset: Int): Flow<List<ChartData>> {

        // 1. Figure out the exact Start and End dates for this specific page
        val (startDate, endDate) = calculateDateRange(filter, offset)

        // 2. Convert standard LocalDates into Unix Milliseconds for the Room Query
        val zoneId = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

        // We go to the very end of the end date (23:59:59)
        val endMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        // 3. Fetch raw data from Room, then immediately pad missing dates
        return runRepository.getStepsAnalytics(startMillis, endMillis, filter.name)
            .map { rawDbResults ->
                // This is the function we wrote earlier to fill in the zeros!
                processChartData(rawDbResults, startDate, endDate, filter)
            }
    }

    /**
     * Helper function to do the calendar math.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDateRange(filter: FilterRange, offset: Int): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()

        return when (filter) {
            FilterRange.WEEK -> {
                val targetWeek = today.plusWeeks(offset.toLong())
                // Assuming your week starts on Monday
                val startOfWeek = targetWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = startOfWeek.plusDays(6)
                Pair(startOfWeek, endOfWeek)
            }
            FilterRange.MONTH -> {
                val targetMonth = today.plusMonths(offset.toLong())
                val startOfMonth = targetMonth.withDayOfMonth(1)
                val endOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth())
                Pair(startOfMonth, endOfMonth)
            }
            FilterRange.YEAR -> {
                val targetYear = today.plusYears(offset.toLong())
                val startOfYear = targetYear.withDayOfYear(1)
                val endOfYear = targetYear.withDayOfYear(targetYear.lengthOfYear())
                Pair(startOfYear, endOfYear)
            }
        }
    }
}