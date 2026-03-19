package com.example.fitnessapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class FitnessApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply { // tells the osmdroid where to store its tile cache
            load(this@FitnessApplication, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
        }
    }
}