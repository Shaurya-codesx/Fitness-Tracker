package com.example.fitnessapp.Data.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.ServiceCompat
import com.example.fitnessapp.Domain.TrackingRunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.R
import com.example.fitnessapp.ui.activity.RunHistory.RunEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {
    @Inject
    lateinit var trackingRepo : TrackingRunRepository
    @Inject
    lateinit var convertTimeUseCase: ConvertTimeUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var runCollectorJob: Job? = null


    companion object {
        const val ACTION_START_RUN = "ACTION_START_RUN"
        const val ACTION_STOP_RUN = "ACTION_STOP_RUN"
    }

    private val CHANNEL_ID = "location_tracking_channel"
    private val NOTIFICATION_ID = 100

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START_RUN -> {
                startRunForeground()
            }
            ACTION_STOP_RUN -> {
                stopRunForeground()
            }
        }

        return START_STICKY // Keep service alive if killed
    }

     fun checkLocationSettings() : Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        return (isGpsEnabled)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startRunForeground() {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification("00:00", "0.00", isPaused = false),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        }catch (e : Exception) {
            stopSelf()
            return
        }
        trackingRepo.startRun() // this calls the android Location provider from the repository and crash happens if location is disabled


        // collecting flow to update the notification live
        runCollectorJob?.cancel()
        runCollectorJob = serviceScope.launch {
            trackingRepo.activeRun.collect { run ->
                if (run == null){
                    runCollectorJob?.cancel()
                    return@collect
                } else{
                    val timeStr = convertTimeUseCase.timerFormat(run.elapsedTime)
                    val distanceStr = String.format("%.2f", run.currentDistance)
                    if (run.trackingStatus) {
                        updateNotification(timeStr, distanceStr, isPaused = false)
                    }
                }
            }
        }

        serviceScope.launch {
            trackingRepo.runEvents.collect { event ->
                when (event) {
                    is RunEvents.LocationDisabled -> {
                        // Change the notification to show a warning!
                        updateNotification("GPS LOST - PAUSED", "Waiting for signal...", isPaused = true)
                    }
                    is RunEvents.LocationRestored -> {
                    }
                    else -> {}
                }
            }
        }
    }

    private fun stopRunForeground() {
        serviceScope.launch {
            trackingRepo.stopRun()
            runCollectorJob?.cancel()
            runCollectorJob = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW // Low so it doesn't "beep" every time
            )
            val manager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(time : String, distance : String, isPaused : Boolean) : Notification {

        // Create a PendingIntent that sends the STOP action to this service
        val stopIntent = Intent(this, LocationForegroundService::class.java).apply {
            action = ACTION_STOP_RUN
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPaused) "⚠️ Run Paused - Check GPS" else "Fitness Tracker"
        val content = if (isPaused) time else "Time: $time s, Distance: $distance m"

        // Notification object
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_notification)
            .setContentTitle("Fitness Tracker")
            .setContentText("Time: $time s, Distance: $distance m")
            .setOngoing(true) // User cannot swipe it away
            .setSilent(true)
            .setVisibility(VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_stop, "Stop Run", stopPendingIntent)
            .build()

    }

    private fun updateNotification(time: String, distance: String, isPaused: Boolean) {
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(time, distance, isPaused))
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}