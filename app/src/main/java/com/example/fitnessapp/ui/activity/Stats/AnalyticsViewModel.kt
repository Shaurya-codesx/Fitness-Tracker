package com.example.fitnessapp.ui.activity.Stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.DistanceSplitCalculator
import com.example.fitnessapp.Domain.UseCases.GetDaysInRange
import com.example.fitnessapp.Domain.UseCases.calculateDateRange
import com.example.fitnessapp.ui.activity.Stats.Distance.DistanceDataUiState
import com.example.fitnessapp.ui.activity.Stats.Distance.processDistanceChartData
import com.example.fitnessapp.ui.activity.Stats.Steps.StepsDataUiState
import com.example.fitnessapp.ui.activity.Stats.Steps.processChartData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val calcDateRange : calculateDateRange,
    private val getDaysInRange: GetDaysInRange,
    private val calcDistanceSplit : DistanceSplitCalculator
) : ViewModel(){

    /**
     * This function will be called by your Compose Pager for every page.
     * offset = 0 is current week/month/year.
     * offset = -1 is the previous week/month/year, etc.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getStepsDataForPage(filter: FilterRange, offset: Int): Flow<StepsDataUiState> {

        // 1. Figure out the exact Start and End dates for this specific page
        val (startDate, endDate) = calcDateRange(filter, offset)

        // 2. Convert standard LocalDates into Unix Milliseconds for the Room Query
        val zoneId = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

        // We go to the very end of the end date (23:59:59)
        val endMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        // 3. Fetch raw data from Room, then immediately pad missing dates
        return runRepository.getStepsAnalytics(startMillis, endMillis, filter.name)
            .map { rawDbResults ->
                // This is the function we wrote earlier to fill in the zeros!
                val chartData = processChartData(rawDbResults, startDate, endDate, filter)

                val totalSteps = chartData.sumOf { it.value }
                val daysInPeriod = getDaysInRange(filter, offset)
                val dailyAverage = if (daysInPeriod > 0) totalSteps / daysInPeriod else 0

                StepsDataUiState(
                    chartData = chartData,
                    dailyAverage = dailyAverage,
                    totalSteps = totalSteps
                )
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDistanceDataForPage(filter: FilterRange, offset: Int): Flow<DistanceDataUiState> {
        val (startDate, endDate) = calcDateRange(filter, offset)
        val zoneId = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1


        // Both DataFlow
        val groupedDataFlow = runRepository.getDistanceAnalytics(startMillis, endMillis, filter.name)
        val rawDistancesFlow = runRepository.getRawDistancesForRange(startMillis, endMillis)

        return combine(groupedDataFlow, rawDistancesFlow) { rawDbResults, rawDistances ->

            // Calculate the bar chart data
            val chartData = processDistanceChartData(rawDbResults, startDate, endDate, filter)
            val totalDistance = chartData.map { it.value }.sum()
            val daysInPeriod = getDaysInRange(filter, offset)
            val dailyAverage = if (daysInPeriod > 0) totalDistance / daysInPeriod else 0f

            // Calculate the donut chart data
            val splitData = calcDistanceSplit(rawDistances)

            DistanceDataUiState(
                chartData = chartData,
                dailyAverage = dailyAverage,
                totalDistance = totalDistance,
                distanceSplit = splitData
            )
        }
    }
}