package com.example.fitnessapp.Domain

import com.example.fitnessapp.Data.Model.Entities.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    fun getUserProfile() : Flow<UserProfile?>

    suspend fun saveUserProfile(name : String, weightKG : Float, heightCM : Int)

    suspend fun deleteUserProfile()
}