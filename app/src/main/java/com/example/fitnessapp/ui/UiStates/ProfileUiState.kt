package com.example.fitnessapp.ui.UiStates

data class ProfileUiState (
    val name: String = "",
    val weight: String = "",
    val height: String = "",
)

sealed class ProfileState {
    object Loading : ProfileState()
    object Empty : ProfileState()
    data class Success(val data: ProfileUiState) : ProfileState()
    data class Error(val message: String) : ProfileState()
}