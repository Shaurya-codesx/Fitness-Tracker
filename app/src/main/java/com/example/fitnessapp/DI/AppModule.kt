package com.example.fitnessapp.DI

import android.content.Context
import android.hardware.SensorManager
import com.example.fitnessapp.Data.Location.androidLocationProvider
import com.example.fitnessapp.Data.Repositories.RunRepoImpl
import com.example.fitnessapp.Data.Repositories.TrackingRepoImpl
import com.example.fitnessapp.Data.Repositories.UserProfileRepoImpl
import com.example.fitnessapp.Data.StepCounter.StepTracker
import com.example.fitnessapp.Data.StepCounter.StepTrackerImplementation
import com.example.fitnessapp.Domain.LocationDataSource
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.TrackingRunRepository
import com.example.fitnessapp.Domain.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    @Binds
    @Singleton
    abstract fun bindLocationDataSource(
        locationProvider: androidLocationProvider // Your implementation
    ): LocationDataSource // The interface your repo asks for
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindRunRepository(
        runRepoImpl: RunRepoImpl
    ): RunRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingRepositoryModule {
    @Binds
    abstract fun bindTrackingRepository(
        trackingRepoImpl: TrackingRepoImpl
    ): TrackingRunRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserProfileRepositoryModule {
    @Binds
    abstract fun bindUserProfileRepository(
        userProfileRepoImpl : UserProfileRepoImpl
    ) : UserProfileRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StepTrackerModule {
    @Binds
    @Singleton
    abstract fun bindStepTracker(
        stepTrackerImplementation: StepTrackerImplementation
    ): StepTracker
}

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {
    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager {
        return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}