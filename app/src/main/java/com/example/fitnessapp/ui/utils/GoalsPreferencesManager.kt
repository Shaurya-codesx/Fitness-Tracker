package com.example.fitnessapp.ui.utils


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates the DataStore instance
private val Context.dataStore by preferencesDataStore(name = "user_goals")

// A clean data class to hold the goals and easily check if they exist
data class UserGoals(
    val stepsGoal: Int? = null,
    val distanceGoalKm: Float? = null,
    val caloriesGoal: Int? = null
) {
    // If any of these are null, the user hasn't completed their setup yet
    val areGoalsSet: Boolean
        get() = stepsGoal != null && distanceGoalKm != null && caloriesGoal != null
}

class GoalsPreferencesManager(private val context: Context) {

    // Define the keys for our DataStore values
    private companion object {
        val STEP_GOAL = intPreferencesKey("step_goal")
        val DISTANCE_GOAL_KM = floatPreferencesKey("distance_goal_km")
        val CALORIES_GOAL = intPreferencesKey("calories_goal")
    }

    // Expose the goals as a Flow so the UI updates instantly when they are edited
    val userGoalsFlow: Flow<UserGoals> = context.dataStore.data.map { preferences ->
        UserGoals(
            stepsGoal = preferences[STEP_GOAL],
            distanceGoalKm = preferences[DISTANCE_GOAL_KM],
            caloriesGoal = preferences[CALORIES_GOAL]
        )
    }

    // Suspend function to save the goals from our upcoming Bottom Sheet
    suspend fun saveGoals(steps: Int, distanceKm: Float, calories: Int) {
        context.dataStore.edit { preferences ->
            preferences[STEP_GOAL] = steps
            preferences[DISTANCE_GOAL_KM] = distanceKm
            preferences[CALORIES_GOAL] = calories
        }
    }

    // Wipes all saved goals on logout
    suspend fun clearGoals() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}