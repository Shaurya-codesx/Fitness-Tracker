package com.example.fitnessapp.Data.Model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnessapp.Data.Model.Entities.UserProfile
import kotlinx.coroutines.flow.Flow


@Dao
interface UserProfileDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(userProfile : UserProfile)

    @Query("SELECT * FROM User_Data WHERE id = 1")
    fun getProfile() : Flow<UserProfile?>

    @Query("DELETE FROM user_data")
    suspend fun deleteProfile()
}