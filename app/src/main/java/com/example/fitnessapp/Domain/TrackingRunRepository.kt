package com.example.fitnessapp.Domain

import com.example.fitnessapp.Data.Model.Entities.ActiveRun
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.ui.activity.RunHistory.RunEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TrackingRunRepository {
    val activeRun : StateFlow<ActiveRun?>
    // stateFlow is a observable data holder that always holds the current value of something, here it holds the current values of the activeRun object and its properties
    // whenever this changes, the observer gets notified of the change and can update itself, so no manual callbacks needed

    /*StateFlow emits new value.             Even if no UI is observing, it still holds state.
    ViewModel receives it.                   That’s important for:
    ViewModel maps to UiState.                  Background tracking, Rotation
    UI re-renders.                              Multiple collectors (UI + notification)*/
    val runEvents : Flow<RunEvents>


    fun startRun()

    suspend fun stopRun()

    fun addLocationPoint(point : LocationPoints)
}