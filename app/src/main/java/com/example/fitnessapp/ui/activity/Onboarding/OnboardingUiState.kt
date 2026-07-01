package com.example.fitnessapp.ui.activity.Onboarding

data class OnboardingUiState(
    val name: String = "",
    val weight: String = "",
    val height: String = "",
    val stepGoal: String = "10000",
    val distanceGoal: String = "5.0",
    val calorieGoal: String = "500",
    val isLoading: Boolean = false
)