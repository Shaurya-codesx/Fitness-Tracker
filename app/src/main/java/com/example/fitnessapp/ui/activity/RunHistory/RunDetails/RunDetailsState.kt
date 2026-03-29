package com.example.fitnessapp.ui.activity.RunHistory.RunDetails

sealed class RunDetailsState {
    object Loading : RunDetailsState()

    data class Success(
        val data: RunDetailsUiState
    ) : RunDetailsState()

    object Error : RunDetailsState()
}