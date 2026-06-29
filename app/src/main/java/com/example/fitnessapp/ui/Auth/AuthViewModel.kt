package com.example.fitnessapp.ui.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.AuthRepository
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
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(_root_ide_package_.com.example.fitnessapp.ui.Auth.AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // NEW: The Flare Gun for Toasts
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

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
                passwordInput = ""
            )
        }
    }

    // ─── FORGOT PASSWORD LOGIC ──────────────────────────────────────────

    fun toggleResetDialog() {
        _uiState.update {
            it.copy(
                showResetDialog = !it.showResetDialog,
                // Pre-fill the reset email if they already started typing it in the main screen
                resetEmailInput = if (!it.showResetDialog) it.emailInput else ""
            )
        }
    }

    fun onResetEmailChange(email: String) {
        _uiState.update { it.copy(resetEmailInput = email) }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.resetEmailInput
        if (email.isBlank()) {
            viewModelScope.launch { _uiEvent.emit("Please enter your email address") }
            return
        }

        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _uiEvent.emit("Reset link sent! Check your inbox.")
                toggleResetDialog() // Close the dialog on success
            }.onFailure { error ->
                _uiEvent.emit(error.localizedMessage ?: "Failed to send reset email")
            }
        }
    }

    // ─── ORIGINAL SUBMIT LOGIC ──────────────────────────────────────────

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