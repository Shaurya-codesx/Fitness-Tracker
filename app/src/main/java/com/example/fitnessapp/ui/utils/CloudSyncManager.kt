package com.example.fitnessapp.ui.utils

import android.util.Log
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Domain.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CloudSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val profileRepo: UserProfileRepository,
    private val goalsManager: GoalsPreferencesManager
) {

    /**
     * 1. THE FETCHER: Pulls data from Firestore and saves it locally.
     * Returns 'true' if the user had existing data (returning user),
     * Returns 'false' if they don't exist in the database (new user).
     */
    suspend fun fetchAndRestoreUserData(): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        return try {
            val document = firestore.collection("users").document(uid).get().await()

            if (document.exists()) {
                // The user exists in the cloud! Pull their exact data down.
                val name = document.getString("name") ?: ""

                // Firestore stores numbers as Doubles/Longs, so we safely cast them back
                val weight = document.getDouble("weight")?.toFloat() ?: 0f
                val height = document.getLong("height")?.toInt() ?: 0

                val steps = document.getLong("stepGoal")?.toInt() ?: 10000
                val distance = document.getDouble("distanceGoal")?.toFloat() ?: 5f
                val calories = document.getLong("calorieGoal")?.toInt() ?: 500

                // Restore everything into the local offline databases
                profileRepo.saveUserProfile(name, weight, height)
                goalsManager.saveGoals(steps, distance, calories)

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 2. THE PUSHER: Sends the local setup data up to Firestore.
     * Call this immediately after they hit "Complete Setup" on the Onboarding Screen.
     */
    suspend fun pushInitialSetupToCloud(
        name: String, weight: Float, height: Int,
        steps: Int, distance: Float, calories: Int
    ) {
        val uid = auth.currentUser?.uid ?: return

        // Bundle it all into a clean HashMap for Firestore
        val userData = hashMapOf(
            "name" to name,
            "weight" to weight,
            "height" to height,
            "stepGoal" to steps,
            "distanceGoal" to distance,
            "calorieGoal" to calories
        )

        // .set() will create the document if it doesn't exist, or overwrite it if it does
        firestore.collection("users").document(uid).set(userData).await()
    }

    /**
     * 3. THE HISTORY RESTORER: Pulls all past runs from Firestore and saves them to Room.
     */
    /**
     * 3. THE HISTORY RESTORER: Pulls all past runs from Firestore and saves them to Room.
     */
    suspend fun fetchAndRestoreRunHistory(runRepo: com.example.fitnessapp.Domain.RunRepository) {
        val uid = auth.currentUser?.uid ?: return

        try {
            Log.e("SYNC_DEBUG", "Starting fetch for UID: $uid")
            val runsSnapshot = firestore.collection("users").document(uid).collection("runs").get().await()

            Log.e("SYNC_DEBUG", "Found ${runsSnapshot.documents.size} runs in Firestore!")
            val runsToRestore = mutableListOf<RunEntity>()

            for (document in runsSnapshot.documents) {
                // Safely cast ANY number type to Long
                val startTime = (document.get("startTime") as? Number)?.toLong()

                if (startTime == null) {
                    Log.e("SYNC_DEBUG", "Failed to parse startTime for document: ${document.id}. Skipping run.")
                    continue
                }

                val endTime = (document.get("endTime") as? Number)?.toLong() ?: 0L
                val distanceInMeters = (document.get("distanceInMeters") as? Number)?.toFloat() ?: 0f
                val stepsTaken = (document.get("stepsTaken") as? Number)?.toInt() ?: 0
                val caloriesBurned = (document.get("caloriesBurned") as? Number)?.toFloat() ?: 0f

                // ─── TRANSLATION MAGIC ───
                val cloudRoute = document.get("route") as? List<Map<String, Any>> ?: emptyList()
                val mappedRoute = cloudRoute.mapNotNull { pointMap ->
                    // Check if it's a GeoPoint OR a custom map
                    val coordinates = pointMap["coordinates"] as? com.google.firebase.firestore.GeoPoint
                    val timeStamp = (pointMap["timeStamp"] as? Number)?.toLong()

                    if (coordinates != null && timeStamp != null) {
                        com.example.fitnessapp.Data.Model.LocationPoints(coordinates, timeStamp)
                    } else {
                        Log.e("SYNC_DEBUG", "Failed to map a route point in run: ${document.id}")
                        null
                    }
                }

                runsToRestore.add(
                    RunEntity(
                        startTime = startTime,
                        endTime = endTime,
                        distanceInMeters = distanceInMeters,
                        stepsTaken = stepsTaken,
                        caloriesBurned = caloriesBurned,
                        route = mappedRoute,
                        isSynced = true
                    )
                )
            }

            Log.e("SYNC_DEBUG", "Successfully parsed ${runsToRestore.size} runs. Inserting to Room...")

            if (runsToRestore.isNotEmpty()) {
                runRepo.insertRuns(runsToRestore)
                Log.e("SYNC_DEBUG", "Insertion complete!")
            }

        } catch (e: Exception) {
            Log.e("SYNC_DEBUG", "CRASH during fetch: ${e.message}")
            e.printStackTrace()
        }
    }

}