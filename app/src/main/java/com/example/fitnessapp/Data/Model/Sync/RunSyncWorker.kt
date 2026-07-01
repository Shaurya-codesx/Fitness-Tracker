package com.example.fitnessapp.Data.Model.Sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters // Replace with your actual DB import
import com.example.fitnessapp.Domain.RunRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class RunSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters, // Injects your Room database
    private val runRepository: RunRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Check if user is logged in. If not, cancel the work.
        val uid = auth.currentUser?.uid ?: return Result.failure()

        return try {
            // 2. Fetch all runs from Room that haven't been uploaded yet
            val unsyncedRuns = runRepository.getUnsyncedRuns()

            if (unsyncedRuns.isEmpty()) {
                return Result.success() // Nothing to do, exit successfully!
            }

            // 3. Prepare a Firestore Batch (allows us to upload multiple runs at exactly the same time)
            val batch = firestore.batch()

            // Reference to this specific user's 'runs' collection
            val userRunsCollection = firestore.collection("users").document(uid).collection("runs")

            // 4. Map the data
            unsyncedRuns.forEach { run ->
                // Create a unique document ID based on the run's start time or Room ID
                val runRef = userRunsCollection.document(run.startTime.toString())

                // TRANSLATION MAGIC: Convert your List<LocationPoints> into a List of Maps for Firestore
                val mappedRoute = run.route.map { locationPoint ->
                    hashMapOf(
                        "coordinates" to locationPoint.coordinates, // Firestore natively understands GeoPoint!
                        "timeStamp" to locationPoint.timeStamp
                    )
                }

                // Map your Room Entity to a Firestore Document
                val runData = hashMapOf(
                    "startTime" to run.startTime,
                    "endTime" to run.endTime,
                    "distanceInMeters" to run.distanceInMeters,
                    "stepsTaken" to run.stepsTaken,
                    "caloriesBurned" to run.caloriesBurned,
                    "route" to mappedRoute // Pass the converted route here
                )

                // Add this specific run to our batch payload
                batch.set(runRef, runData)
            }

            // 5. Fire the batch to the cloud!
            batch.commit().await()

            // 6. If the upload succeeds, update Room so we don't upload them again tomorrow
            val syncedRuns = unsyncedRuns.map { it.copy(isSynced = true) }
            runRepository.updateRuns(syncedRuns)

            Result.success()

        } catch (e: Exception) {
            // If the network drops while uploading, tell WorkManager to try again later
            Result.retry()
        }
    }
}