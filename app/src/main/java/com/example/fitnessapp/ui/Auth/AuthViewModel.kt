package com.example.fitnessapp.ui.Auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.AuthRepository
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.ui.utils.CloudSyncManager
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
    private val authRepository: AuthRepository,
    private val runRepo : RunRepository,
    private val cloudSyncManager: CloudSyncManager // Injected for offline-first routing
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(emailInput = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(passwordInput = password, errorMessage = null) }
    }

    // ─── RESTORED: Toggle Mode ───
    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                errorMessage = null,
                passwordInput = ""
            )
        }
    }

    // ─── RESTORED: Forgot Password Logic ───
    fun toggleResetDialog() {
        _uiState.update {
            it.copy(
                showResetDialog = !it.showResetDialog,
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
                toggleResetDialog()
            }.onFailure { error ->
                _uiEvent.emit(error.localizedMessage ?: "Failed to send reset email")
            }
        }
    }

    // ─── SUBMIT LOGIC (With Cloud Sync Routing) ───
    // ─── SUBMIT LOGIC (With Cloud Sync Routing) ───
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
                if (state.isLoginMode) {
                    val isReturningUser = cloudSyncManager.fetchAndRestoreUserData()

                    if (isReturningUser) {
                        // FIX: We removed the nested 'launch' block!
                        // Now, this suspend function blocks the execution here until it finishes downloading EVERYTHING.
                        cloudSyncManager.fetchAndRestoreRunHistory(runRepo)

                        // Only AFTER the database is fully populated, we navigate.
                        _uiEvent.emit("ReturningUser")
                    } else {
                        _uiEvent.emit("NewUser")
                    }
                } else {
                    _uiEvent.emit("NewUser")
                }

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Authentication failed")
                }
            }
        }
    }
}