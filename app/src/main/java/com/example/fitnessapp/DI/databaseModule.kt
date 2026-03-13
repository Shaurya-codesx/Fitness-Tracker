package com.example.fitnessapp.DI

import android.content.Context
import com.example.fitnessapp.Data.Model.AppDatabase
import com.example.fitnessapp.Data.Model.runDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object databaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context : Context) : AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideRunDAO(db : AppDatabase) : runDAO {
        return db.getDAO()
    }
}