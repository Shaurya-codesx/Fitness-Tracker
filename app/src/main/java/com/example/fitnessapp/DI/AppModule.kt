package com.example.fitnessapp.DI

import com.example.fitnessapp.Data.Location.androidLocationProvider
import com.example.fitnessapp.Domain.LocationDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
