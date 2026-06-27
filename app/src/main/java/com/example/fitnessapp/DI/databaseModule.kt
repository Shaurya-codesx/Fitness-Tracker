package com.example.fitnessapp.DI

import android.content.Context
import com.example.fitnessapp.Data.Model.AppDatabase
import com.example.fitnessapp.Data.Model.UserProfileDAO
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.ui.utils.GoalsPreferencesManager
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

    @Provides
    fun provideUserProfileDAO(db : AppDatabase) : UserProfileDAO {
        return db.getUserProfileDAO()
    }

    @Provides
    @Singleton
    fun provideGoalsPreferencesManager(
        @ApplicationContext context: Context
    ): GoalsPreferencesManager {
        return GoalsPreferencesManager(context)
    }

}