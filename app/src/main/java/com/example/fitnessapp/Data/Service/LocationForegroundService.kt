package com.example.fitnessapp.Data.Service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.PermissionChecker
import com.example.fitnessapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationForegroundService : Service() {

    private val CHANNEL_ID = "location_tracking_channel"
    private val NOTIFICATION_ID = 100

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel() // Create channel first
        startForeground()   // Then promote to foreground

        return START_STICKY // Keep service alive if killed
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startForeground() {
        // before starting the service as foreground we check if the permission it requires are enabled
        val locationPermission = PermissionChecker.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (locationPermission != PermissionChecker.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_notification)
            .setContentTitle("Fitness Tracker")
            .setContentText("Tracking your run...")
            .setOngoing(true) // User cannot swipe it away
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            ServiceCompat.startForeground(
                this,
                100,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        }catch (e : Exception) {
            stopSelf()
            return
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW // Low so it doesn't "beep" every time
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}