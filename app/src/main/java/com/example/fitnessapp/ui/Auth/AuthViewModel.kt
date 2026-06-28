package com.example.fitnessapp.ui.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailInput = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(passwordInput = password, errorMessage = null) }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                errorMessage = null,
                passwordInput = "" // Clear password on switch for security
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.emailInput.isBlank() || state.passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Fields cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (state.isLoginMode) {
                authRepository.signIn(state.emailInput, state.passwordInput)
            } else {
                authRepository.signUp(state.emailInput, state.passwordInput)
            }

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Authentication failed")
                }
            }
        }
    }
}