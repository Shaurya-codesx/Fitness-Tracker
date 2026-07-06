package com.example.fitnessapp.ui.utils

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
    suspend fun fetchAndRestoreRunHistory(runRepo: com.example.fitnessapp.Domain.RunRepository) {
        val uid = auth.currentUser?.uid ?: return

        try {
            val runsSnapshot = firestore.collection("users").document(uid).collection("runs").get().await()
            val runsToRestore = mutableListOf<RunEntity>()

            for (document in runsSnapshot.documents) {
                val startTime = document.getLong("startTime")
                if (startTime == null) {
                    continue
                }

                val endTime = document.getLong("endTime") ?: 0L
                val distanceInMeters = document.getDouble("distanceInMeters")?.toFloat() ?: 0f
                val stepsTaken = document.getLong("stepsTaken")?.toInt() ?: 0
                val caloriesBurned = document.getDouble("caloriesBurned")?.toFloat() ?: 0f

                // ─── TRANSLATION MAGIC ───
                val cloudRoute = document.get("route") as? List<Map<String, Any>> ?: emptyList()
                val mappedRoute = cloudRoute.mapNotNull { pointMap ->
                    val coordinates = pointMap["coordinates"] as? com.google.firebase.firestore.GeoPoint
                    // NOTE: Sometimes Firestore stores whole numbers as Longs and decimals as Doubles.
                    // If your timestamp was saved differently, this cast might fail silently.
                    val timeStamp = (pointMap["timeStamp"] as? Number)?.toLong()

                    if (coordinates != null && timeStamp != null) {
                        com.example.fitnessapp.Data.Model.LocationPoints(coordinates, timeStamp)
                    } else {
                        null // Fails silently here if mapping is wrong
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
            if (runsToRestore.isNotEmpty()) {
                runRepo.insertRuns(runsToRestore)
            } else {
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}