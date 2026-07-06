package com.example.fitnessapp.ui.activity.Onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.UserProfileRepository // Adjust import if needed
import com.example.fitnessapp.ui.utils.CloudSyncManager
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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.core.content.ContextCompat


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepo: UserProfileRepository,
    private val goalsManager: GoalsPreferencesManager, // Your DataStore manager
    private val cloudSyncManager: CloudSyncManager // <-- 1. Inject the new sync manager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    // ─── INPUT HANDLERS (Keep your exact input filtering logic) ───
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

    // ─── SAVE & PUSH LOGIC ───
    fun completeOnboarding() {
        val state = _uiState.value

        val parsedWeight = state.weight.toFloatOrNull() ?: 0f
        val parsedHeight = state.height.toIntOrNull() ?: 0
        val parsedSteps = state.stepGoal.toIntOrNull() ?: 0
        val parsedDistance = state.distanceGoal.toFloatOrNull() ?: 0f
        val parsedCalories = state.calorieGoal.toIntOrNull() ?: 0

        if (state.name.isBlank() || parsedWeight <= 0f || parsedHeight <= 0 ||
            parsedSteps <= 0 || parsedDistance <= 0f || parsedCalories <= 0) {
            viewModelScope.launch { _uiEvent.emit("Please fill out all fields with valid numbers.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // a) Save locally to Room and DataStore
                profileRepo.saveUserProfile(state.name, parsedWeight, parsedHeight)
                goalsManager.saveGoals(parsedSteps, parsedDistance, parsedCalories)

                // b) <-- 2. Push to Firestore cloud instantly!
                cloudSyncManager.pushInitialSetupToCloud(
                    name = state.name,
                    weight = parsedWeight,
                    height = parsedHeight,
                    steps = parsedSteps,
                    distance = parsedDistance,
                    calories = parsedCalories
                )

                _uiEvent.emit("Success") // Signal UI to navigate to Home
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _uiEvent.emit("Failed to save data. Please try again.")
            }
        }
    }
}