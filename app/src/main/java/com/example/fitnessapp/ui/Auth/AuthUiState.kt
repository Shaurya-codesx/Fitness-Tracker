package com.example.fitnessapp.ui.Auth

data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoginMode: Boolean = true, // True = Login, False = Register
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val showResetDialog: Boolean = false,
    val resetEmailInput: String = ""
)