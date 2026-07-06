package com.example.fitnessapp.ui.activity.HomeScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UserProfileRepository
import com.example.fitnessapp.ui.UiStates.HomeUiState
import com.example.fitnessapp.ui.utils.GoalsPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val runRepository: RunRepository, 
    private val userProfileRepository: UserProfileRepository, // Or UserDao if getUserProfile is there
    private val goalsManager: GoalsPreferencesManager
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    val homeUiState: Flow<HomeUiState> = createHomeFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createHomeFlow(): Flow<HomeUiState> {
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()

        // Calculate milliseconds for the very start and very end of today
        val startOfDay = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        return combine(
            userProfileRepository.getUserProfile(),
            runRepository.getTodayStats(startOfDay, endOfDay),
            runRepository.getAllRunStartTimes(),
            goalsManager.userGoalsFlow
        ) { profile, todayStats, runDates, goals ->

            // 1. Extract User Name (Default to "Runner" if profile isn't set yet)
            val name = profile?.name ?: "Runner"

            // 2. Safely extract today's stats (defaults to 0 if no runs today)
            val steps = todayStats?.totalSteps ?: 0
            val distanceKm = (todayStats?.totalDistanceMeters ?: 0f) / 1000f
            val calories = todayStats?.totalCalories?.toInt() ?: 0

            // 3. Calculate Streak and Heatmap data
            val streak = calculateStreak(runDates)
            val activeDays = calculateHeatmapDays(runDates)

            HomeUiState.Success(
                userName = name,
                currentStreak = streak,
                todaySteps = steps,
                todayDistanceKm = distanceKm,
                todayCalories = calories,
                userGoals = goals,
                activeDaysThisMonth = activeDays
            ) as HomeUiState
        }
            .onStart { emit(HomeUiState.Loading) }
            .catch { e -> emit(HomeUiState.Error(e.message ?: "An unknown error occurred")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = HomeUiState.Loading
            )
    }

    // ─── HELPER FUNCTIONS ──────────────────────────────────────────────────

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateStreak(startTimes: List<Long>): Int {
        if (startTimes.isEmpty()) return 0

        val zoneId = ZoneId.systemDefault()
        // Convert to unique local dates, sorted descending (newest first)
        val activeDates = startTimes.map {
            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
        }.toSortedSet(Comparator.reverseOrder())

        var streak = 0
        var currentDate = LocalDate.now()

        // If they haven't run today, check if they ran yesterday. 
        // If not, the streak is completely broken (0).
        if (!activeDates.contains(currentDate)) {
            currentDate = currentDate.minusDays(1)
            if (!activeDates.contains(currentDate)) {
                return 0
            }
        }

        // Count consecutively backwards
        for (date in activeDates) {
            if (date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (date.isBefore(currentDate)) {
                break // A gap was found!
            }
        }
        return streak
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateHeatmapDays(startTimes: List<Long>): Set<LocalDate> {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()

        return startTimes.map {
            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
        }.filter {
            it.month == today.month && it.year == today.year
        }.toSet()
    }

    // Function exposed to the UI to save new goals
    fun saveNewGoals(steps: Int, distanceKm: Float, calories: Int) {
        viewModelScope.launch {
            goalsManager.saveGoals(steps, distanceKm, calories)
        }
    }
}
