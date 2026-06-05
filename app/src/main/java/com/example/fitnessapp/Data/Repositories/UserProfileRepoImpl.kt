package com.example.fitnessapp.Data.Repositories

import com.example.fitnessapp.Data.Model.Entities.UserProfile
import com.example.fitnessapp.Data.Model.UserProfileDAO
import com.example.fitnessapp.Domain.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserProfileRepoImpl @Inject constructor(
    private val userProfileDao : UserProfileDAO
) : UserProfileRepository {
    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getProfile()
    }

    override suspend fun saveUserProfile(
        name : String,
        weightKG : Float,
        heightCM : Int,
    ) {
        userProfileDao.saveProfile(
            UserProfile(
                name =  name,
                weightKG = weightKG,
                heightCM = heightCM,
            )
        )
    }
}