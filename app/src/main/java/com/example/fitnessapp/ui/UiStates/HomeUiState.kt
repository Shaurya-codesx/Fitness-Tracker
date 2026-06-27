package com.example.fitnessapp.ui.UiStates

import com.example.fitnessapp.ui.utils.UserGoals
import java.time.LocalDate

sealed interface HomeUiState {
    object Loading : HomeUiState

    data class Success(
        val userName: String,
        val currentStreak: Int,
        val todaySteps: Int,
        val todayDistanceKm: Float,
        val todayCalories: Int,
        val userGoals: UserGoals, // Contains the goals and the .areGoalsSet boolean!
        val activeDaysThisMonth: Set<LocalDate> // For the Heatmap
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}