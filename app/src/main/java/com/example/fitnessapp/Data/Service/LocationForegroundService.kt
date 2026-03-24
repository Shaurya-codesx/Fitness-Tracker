package com.example.fitnessapp.Data.Service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.ServiceCompat
import androidx.core.content.PermissionChecker
import com.example.fitnessapp.Data.Repositories.RunRepoImpl
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.R
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
    lateinit var runRepo : RunRepository
    @Inject
    lateinit var convertTimeUseCase: ConvertTimeUseCase

    /*
    to start this service
        val intent = Intent(context, LocationForegroundService::class.java)
        intent.action = LocationForegroundService.ACTION_START_RUN
        ContextCompat.startForegroundService(context, intent)

    to stop this service
        val intent = Intent(context, LocationForegroundService::class.java)
        intent.action = LocationForegroundService.ACTION_STOP_RUN
        context.startService(intent)
    */


    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default) // to launch the stoprun function

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
                Log.d("servicee", "Starting Location Foreground Service")
            }
            ACTION_STOP_RUN -> {
                stopRunForeground()
                Log.d("servicee", "Stoping Location Foreground Service")
            }
        }

        return START_STICKY // Keep service alive if killed
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startRunForeground() {
        // before starting the service as foreground we check if the permission it requires are enabled
        val locationPermission = PermissionChecker.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (locationPermission != PermissionChecker.PERMISSION_GRANTED) {
            stopSelf()
            return
        }



        Log.d("servicee", "notification built")

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification("00:00", "0.00"),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
            Log.d("servicee", "promoted to foreground")
        }catch (e : Exception) {
            stopSelf()
            return
        }
        runRepo.startRun()

        // collecting flow to update the notification live
        runCollectorJob?.cancel()
        runCollectorJob = serviceScope.launch {
            runRepo.activeRun.collect { run ->
                if (run == null){
                    runCollectorJob?.cancel()
                    return@collect
                } else{
                    val timeStr = convertTimeUseCase.timerFormat(run.elapsedTime)
                    val distanceStr = String.format("%.2f", run.currentDistance)
                    updateNotification(timeStr, distanceStr)
                    Log.d("servicee", "run received jsut now")
                }
            }
        }
        Log.d("servicee", "run started")
    }

    private fun stopRunForeground() {
        serviceScope.launch {
            runRepo.stopRun()
            runCollectorJob?.cancel()
            runCollectorJob = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        Log.d("servicee", "run stopped")
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

    private fun buildNotification(time : String, distance : String) : Notification {

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

    private fun updateNotification(time: String, distance: String) {
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(time, distance))
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}