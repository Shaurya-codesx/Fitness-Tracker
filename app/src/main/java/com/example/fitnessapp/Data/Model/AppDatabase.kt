package com.example.fitnessapp.Data.Model

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.Entities.UserProfile

@Database(entities = [RunEntity::class, UserProfile::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { // inherits from RoomDatabase class
    abstract fun getDAO() : runDAO

    abstract fun getUserProfileDAO() : UserProfileDAO



    companion object{
        @Volatile
        private var INSTANCE : AppDatabase ?= null
        fun getDatabase(context: Context) : AppDatabase{
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appDatabase",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also {
                    INSTANCE = it
                }
            }
        }
    }
}