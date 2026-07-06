package com.example.fitnessapp.Domain


// 1. The Interface
interface AuthRepository {
    val currentUserUid: String?

    suspend fun signUp(email: String, password: String): Result<String>

    suspend fun signIn(email: String, password: String): Result<String>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun logOut()
}

