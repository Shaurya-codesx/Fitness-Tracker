package com.example.fitnessapp.Data.Model.Entities

import androidx.compose.ui.text.font.FontWeight
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "User_Data")
data class UserProfile (
    @PrimaryKey(autoGenerate = true)
    val id : Int = 1,
    val name : String,
    val weightKG: Float,
    val heightCM: Int
)