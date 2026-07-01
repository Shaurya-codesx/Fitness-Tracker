package com.example.fitnessapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fitnessapp.Data.Model.Sync.RunSyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class FitnessApplication : Application(), androidx.work.Configuration.Provider {

    // 1. Inject the Hilt Worker Factory so WorkManager can use your Room DB and Firestore
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // 2. Tell WorkManager to use Hilt to build its workers
    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // 3. Your existing OSMDroid setup (Kept exactly as you had it)
        Configuration.getInstance().apply {
            load(this@FitnessApplication, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
        }

        // 4. Setup the constraints: Only run the sync when connected to the internet
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 5. Schedule the worker to run periodically (every 15 minutes is the Android minimum)
        val syncWorkRequest = PeriodicWorkRequestBuilder<RunSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        // 6. Enqueue it! KEEP ensures it doesn't create duplicates if the app restarts
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "RunSyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}