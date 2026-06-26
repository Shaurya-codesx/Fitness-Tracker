package com.example.fitnessapp.ui.activity.Stats

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.DistanceSplitCalculator
import com.example.fitnessapp.Domain.UseCases.EnergySplitCalculator
import com.example.fitnessapp.Domain.UseCases.GetDaysInRange
import com.example.fitnessapp.Domain.UseCases.PaceFormatterUseCase
import com.example.fitnessapp.Domain.UseCases.PaceSplitCalculator
import com.example.fitnessapp.Domain.UseCases.calculateDateRange
import com.example.fitnessapp.ui.UiStates.PersonalBestUiState
import com.example.fitnessapp.ui.activity.Stats.Distance.DistanceDataUiState
import com.example.fitnessapp.ui.activity.Stats.Distance.processDistanceChartData
import com.example.fitnessapp.ui.activity.Stats.Energy.EnergyUiState
import com.example.fitnessapp.ui.activity.Stats.Energy.processEnergyChartData
import com.example.fitnessapp.ui.activity.Stats.Pace.PaceDataUiState
import com.example.fitnessapp.ui.activity.Stats.Pace.processPaceChartData
import com.example.fitnessapp.ui.activity.Stats.Steps.StepsDataUiState
import com.example.fitnessapp.ui.activity.Stats.Steps.processChartData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val calcDateRange : calculateDateRange,
    private val getDaysInRange: GetDaysInRange,
    private val calcDistanceSplit : DistanceSplitCalculator,
    private val calcPaceSplit : PaceSplitCalculator,
    private val calcEnergySplit : EnergySplitCalculator
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPaceDataForPage(filter: FilterRange, offset: Int): Flow<PaceDataUiState> {
        val (startDate, endDate) = calcDateRange(filter, offset)
        val zoneId = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        // 1. Get both flows
        val groupedDataFlow = runRepository.getPaceAnalytics(startMillis, endMillis, filter.name)
        val rawRunsFlow = runRepository.getRawRunsForRange(startMillis, endMillis)

        // 2. Combine them
        return combine(groupedDataFlow, rawRunsFlow) { rawDbResults, rawRuns ->

            // Calculate line chart data
            val chartData = processPaceChartData(rawDbResults, startDate, endDate, filter)

            val totalTimeMillis = rawDbResults.sumOf { it.totalDuration }
            val totalDistanceMeters = rawDbResults.map { it.totalDistance }.sum()

            val overallAveragePace = if (totalDistanceMeters > 0f) {
                val totalMinutes = totalTimeMillis / 60000f
                val totalKm = totalDistanceMeters / 1000f
                totalMinutes / totalKm
            } else {
                0f
            }

            // Calculate donut chart data
            val splitData = calcPaceSplit(rawRuns)

            PaceDataUiState(
                chartData = chartData,
                averagePaceDecimal = overallAveragePace,
                paceSplit = splitData // Pass the split data to the UI
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEnergyDataForPage(filter: FilterRange, offset: Int): Flow<EnergyUiState> {
        val (startDate, endDate) = calcDateRange(filter, offset)
        val zoneId = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val groupedDataFlow = runRepository.getEnergyAnalytics(startMillis, endMillis, filter.name)
        val rawCaloriesFlow = runRepository.getRawCaloriesForRange(startMillis, endMillis)

        return combine(groupedDataFlow, rawCaloriesFlow) { rawDbResults, rawCalories ->

            // 1. Process Bar Chart Data
            // (You'll need a processEnergyChartData helper just like processDistanceChartData,
            // but reading from the EnergyModel's totalCalories)
            val chartData = processEnergyChartData(rawDbResults, startDate, endDate, filter)

            // 2. Calculate Averages
            val totalCalories = chartData.map { it.value }.sum()
            val daysInPeriod = getDaysInRange(filter, offset)
            val dailyAverage = if (daysInPeriod > 0) totalCalories / daysInPeriod else 0f

            // 3. Calculate the Workout Intensity Split
            val splitData = calcEnergySplit(rawCalories)

            EnergyUiState(
                chartData = chartData,
                dailyAverage = dailyAverage,
                totalCalories = totalCalories,
                energySplit = splitData
            )
        }
    }

    fun getPersonalBests(): Flow<PersonalBestUiState> {
        return combine(
            runRepository.getRecordDistance(),
            runRepository.getRecordDuration(),
            runRepository.getRecordCalories(),
            runRepository.getRecordSteps(),
            runRepository.getRecordPaceRun()
        ) { distance, duration, calories, steps, fastestRun ->

            // If all core metrics are null, the user has absolutely no runs logged yet.
            if (distance == null && duration == null && calories == null && steps == null) {
                return@combine PersonalBestUiState.Empty
            }

            // 1. Format Distance (Meters to Kilometers)
            val distStr = if (distance != null && distance > 0f) {
                String.format(Locale.getDefault(), "%.2f km", distance / 1000f)
            } else "–"

            // 2. Format Duration (Millis to "Xh Ym")
            val durStr = if (duration != null && duration > 0L) {
                val hours = TimeUnit.MILLISECONDS.toHours(duration)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
                if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
            } else "–"

            // 3. Format Calories (Float to Int)
            val calStr = if (calories != null && calories > 0f) {
                "${calories.toInt()} kcal"
            } else "–"

            // 4. Format Steps
            val stepStr = if (steps != null && steps > 0) {
                "%,d steps".format(Locale.getDefault(), steps)
            } else "–"

            // 5. Format Pace (Using your existing Pace math!)
            val paceStr = if (fastestRun != null && fastestRun.distanceMeters > 0f) {
                val durationMins = fastestRun.durationMillis / 60000f
                val distanceKm = fastestRun.distanceMeters / 1000f
                val paceDecimal = durationMins / distanceKm
                PaceFormatterUseCase.formatDecimalPaceToString(paceDecimal) + " /km"
            } else "–"

            // Return the bundled Success state
            PersonalBestUiState.Success(
                recordDistance = distStr,
                recordDuration = durStr,
                recordCalories = calStr,
                recordSteps = stepStr,
                recordPace = paceStr
            )
        }
            .onStart { emit(PersonalBestUiState.Loading) }
            .catch { e ->
                emit(PersonalBestUiState.Error(e.message ?: "An unexpected database error occurred"))
            }
    }
}