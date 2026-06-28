package com.example.fitnessapp.ui.activity.UserProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.AuthRepository
import com.example.fitnessapp.Domain.UserProfileRepository
import com.example.fitnessapp.ui.UiStates.ProfileState
import com.example.fitnessapp.ui.UiStates.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: UserProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    // 1. THE EVENT FLARE GUN: Used to send Toasts to the UI safely
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    // 2. THE FIX: Prevents the ViewModel from constantly overwriting the UI when you delete the name
    private var isFormInitialized = false

    val profileState: StateFlow<ProfileState> =
        profileRepo.getUserProfile()
            .onStart { emit(null) }
            .combine(_uiState) { profile, currentUiState ->
                if (profile == null || profile.name.isBlank()) {
                    ProfileState.Empty
                } else {
                    if (!isFormInitialized) {
                        _uiState.update {
                            it.copy(
                                name = profile.name,
                                weight = profile.weightKG.toString(),
                                height = profile.heightCM.toString()
                            )
                        }
                        isFormInitialized = true // Mark as initialized so it never overwrites again!
                    }
                    ProfileState.Success(
                        ProfileUiState(
                            name = profile.name,
                            weight = profile.weightKG.toString(),
                            height = profile.heightCM.toString()
                        )
                    )
                }
            }
            .catch { emit(ProfileState.Error("Something went wrong")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProfileState.Loading
            )

    fun onNameChange(newName: String) {
        val filteredName = newName.filter { it.isLetter() || it.isWhitespace() }
        _uiState.update { it.copy(name = filteredName) }
    }

    fun onWeightChange(newWeight: String) {
        val filteredWeight = newWeight.filter { it.isDigit() || it == '.' }
        if (filteredWeight.count { it == '.' } <= 1) {
            _uiState.update { it.copy(weight = filteredWeight) }
        }
    }

    fun onHeightChange(newHeight: String) {
        val filteredHeight = newHeight.filter { it.isDigit() }
        _uiState.update { it.copy(height = filteredHeight) }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val weight = currentState.weight.toFloatOrNull() ?: 0f
        val height = currentState.height.toIntOrNull() ?: 0

        // 3. THE VALIDATION: Block the save and fire the Toast if data is bad
        if (currentState.name.isBlank() || weight <= 0f || height <= 0) {
            viewModelScope.launch {
                _uiEvent.emit("Please fill all fields with valid numbers above 0")
            }
            return // Instantly stop the function
        }

        viewModelScope.launch {
            try {
                profileRepo.saveUserProfile(name = currentState.name, weightKG = weight, heightCM = height)
                _uiEvent.emit("Success") // Signal the UI that the save worked!
            } catch (e: Exception) {
                _uiEvent.emit("Failed to save profile")
            }
        }
    }

    fun logOut(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logOut()
            onLogoutComplete()
        }
    }
}