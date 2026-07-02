package com.example.fitnessapp.Data.Repositories

import android.util.Log
import com.example.fitnessapp.Data.Model.AppDatabase
import com.example.fitnessapp.Domain.AuthRepository
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UserProfileRepository
import com.example.fitnessapp.ui.utils.GoalsPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// 2. The Implementation
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userProfileRepo: UserProfileRepository,
    private val runRepo : RunRepository,
    private val goalsManager: GoalsPreferencesManager // Injecting DataStore
) : AuthRepository {

    override val currentUserUid: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Account created but user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user.uid)
            } else {
                Result.failure(Exception("Sign in successful but user is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logOut() {
        // 1. Sign out of Firebase
        firebaseAuth.signOut()

        // 2. Wipe the local Room database
        userProfileRepo.deleteUserProfile()
        runRepo.deleteRuns()
        Log.d("logout", "all data deleted")

        // 3. Wipe the local DataStore goals
        goalsManager.clearGoals()
    }
}