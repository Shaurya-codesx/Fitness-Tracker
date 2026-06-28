package com.example.fitnessapp.Domain

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// 1. The Interface
interface AuthRepository {
    val currentUserUid: String?

    suspend fun signUp(email: String, password: String): Result<String>

    suspend fun signIn(email: String, password: String): Result<String>

    suspend fun logOut()
}

