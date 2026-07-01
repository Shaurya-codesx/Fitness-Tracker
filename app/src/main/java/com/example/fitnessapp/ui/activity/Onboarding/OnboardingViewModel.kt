package com.example.fitnessapp.ui.activity.Onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.UserProfileRepository // Adjust import if needed
import com.example.fitnessapp.ui.utils.GoalsPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepo: UserProfileRepository,
    private val goalsManager: GoalsPreferencesManager // Your DataStore manager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    // ─── INPUT HANDLERS (With strict filtering) ──────────────────────────
    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _uiState.update { it.copy(name = filtered) }
    }
    fun onWeightChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) _uiState.update { it.copy(weight = filtered) }
    }
    fun onHeightChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(height = filtered) }
    }
    fun onStepGoalChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(stepGoal = filtered) }
    }
    fun onDistanceGoalChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) _uiState.update { it.copy(distanceGoal = filtered) }
    }
    fun onCalorieGoalChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(calorieGoal = filtered) }
    }

    // ─── SAVE LOGIC ──────────────────────────────────────────────────────
    fun completeOnboarding() {
        val state = _uiState.value

        // 1. Safe Parsing
        val parsedWeight = state.weight.toFloatOrNull() ?: 0f
        val parsedHeight = state.height.toIntOrNull() ?: 0
        val parsedSteps = state.stepGoal.toIntOrNull() ?: 0
        val parsedDistance = state.distanceGoal.toFloatOrNull() ?: 0f
        val parsedCalories = state.calorieGoal.toIntOrNull() ?: 0

        // 2. Strict Validation
        if (state.name.isBlank() || parsedWeight <= 0f || parsedHeight <= 0 ||
            parsedSteps <= 0 || parsedDistance <= 0f || parsedCalories <= 0) {
            viewModelScope.launch { _uiEvent.emit("Please fill out all fields with valid numbers.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        // 3. Save to Room and DataStore simultaneously
        viewModelScope.launch {
            try {
                profileRepo.saveUserProfile(state.name, parsedWeight, parsedHeight)
                goalsManager.saveGoals(parsedSteps, parsedDistance, parsedCalories)

                _uiEvent.emit("Success") // Signal UI to navigate to Home
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.emit("Failed to save data. Please try again.")
            }
        }
    }
}