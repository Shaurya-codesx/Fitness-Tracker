package com.example.fitnessapp.Domain.Notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnessapp.Domain.RunRepository // Adjust to your actual import
import com.example.fitnessapp.ui.activity.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val runRepository: RunRepository // We inject this to check their history!
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        // We will put the database check and logic here in the next step!
        try {
            // 1. Get the most recent run from the local Room database
            val latestRun = runRepository.getLatestRun()

            // If they have never logged a run, we just exit silently
            if (latestRun == null) {
                return Result.success()
            }

            // 2. Convert timestamps to local calendar days
            val lastRunDate = Instant.ofEpochMilli(latestRun.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val today = LocalDate.now()

            // 3. Calculate how many midnights have passed
            val daysSinceLastRun = ChronoUnit.DAYS.between(lastRunDate, today)

            Log.d("CoachWorker", "Days since last run: $daysSinceLastRun")

            // 4. Fire the correct notification based on our product rules
            when (daysSinceLastRun) {
                0L -> {
                    // They already ran today! Do nothing, don't spam them.
                    Log.d("CoachWorker", "User ran today. No notification sent.")
                }
                1L -> {
                    // They ran yesterday, but not today. The streak is in danger!
                    showNotification(
                        title = "Keep the Momentum! 🔥",
                        message = "A quick 10-minute walk is all it takes to keep your streak alive today."
                    )
                }
                3L -> {
                    // It has been exactly 3 days. A gentle nudge.
                    showNotification(
                        title = "Time to lace up! 👟",
                        message = "Rest days are important, but we miss you out there. Ready for a quick run?"
                    )
                }
                // If it's 2 days, or 4+ days, we stay silent to avoid being annoying.
            }

            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry() // Try again later if the database was locked
        }
    }

    // A helper function to actually build and show the notification
    private fun showNotification(title: String, message: String) {
        // Check if the user granted notification permissions (Required for Android 13+)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Make the notification open your app when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification targeting our new "coach_channel"
        val notification = NotificationCompat.Builder(context, "coach_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app's icon later!
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismisses when clicked
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}