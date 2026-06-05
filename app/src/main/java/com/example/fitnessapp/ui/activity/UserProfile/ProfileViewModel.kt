package com.example.fitnessapp.ui.activity.UserProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.UserProfileRepository
import com.example.fitnessapp.ui.UiStates.ProfileState
import com.example.fitnessapp.ui.UiStates.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    // This holds the temporary state of the text fields while editing
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    // The state of the data coming from the database
    val profileState: StateFlow<ProfileState> =
        profileRepo.getUserProfile()
            .onStart {
                emit(null) // Trigger loading state
            }
            .combine(_uiState) { profile, currentUiState ->
                if (profile == null) {
                    ProfileState.Empty
                } else {
                    // When data first loads, if our local state is empty, populate it
                    if (currentUiState.name.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                name = profile.name,
                                weight = profile.weightKG.toString(),
                                height = profile.heightCM.toString()
                            )
                        }
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
            .catch {
                emit(ProfileState.Error("Something went wrong"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProfileState.Loading
            )

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onWeightChange(newWeight: String) {
        _uiState.update { it.copy(weight = newWeight) }
    }

    fun onHeightChange(newHeight: String) {
        _uiState.update { it.copy(height = newHeight) }
    }

    fun saveProfile() {
        val currentState = _uiState.value

        // Basic validation and parsing
        val weight = currentState.weight.toFloatOrNull() ?: 0f
        val height = currentState.height.toIntOrNull() ?: 0

        viewModelScope.launch {
            try {
                profileRepo.saveUserProfile(
                    name = currentState.name,
                    weightKG = weight,
                    heightCM = height
                )
                // You could add a "Save Success" event here if needed
            } catch (e: Exception) {
                // Handle error during save
            }
        }
    }
}