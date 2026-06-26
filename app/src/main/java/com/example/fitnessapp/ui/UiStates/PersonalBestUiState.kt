package com.example.fitnessapp.ui.UiStates

sealed interface PersonalBestUiState {
    object Loading : PersonalBestUiState
    object Empty : PersonalBestUiState
    data class Success(
        val recordDistance: String,
        val recordDuration: String,
        val recordCalories: String,
        val recordSteps: String,
        val recordPace: String
    ) : PersonalBestUiState
    data class Error(val message: String) : PersonalBestUiState
}