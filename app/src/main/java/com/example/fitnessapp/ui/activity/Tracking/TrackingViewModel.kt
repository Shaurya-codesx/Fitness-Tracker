package com.example.fitnessapp.ui.activity.Tracking

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Data.Service.LocationForegroundService
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.Domain.UseCases.PaceCalcUseCase
import com.example.fitnessapp.ui.UiStates.TrackingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val runRepo : RunRepository,
    private val convertTimeUseCase: ConvertTimeUseCase,
    private val paceCalcUseCase: PaceCalcUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // this collects the activeRun stateFlow from the repo and translates it into TrackingUiState and exposes it to the UI
    // but activeRun is already stateFlow, but that is a domain model, we need a UI only model, with modified values
    // so this viewModel takes the domain model and converts it into presentation model and exposes it to the UI


    // it takes in the cold StateFlow from the repo and converts it into a hot StateFlow that is exposed to the UI
    val trackingUiState: StateFlow<TrackingUiState> = runRepo.activeRun
        .onEach { run ->
            Log.d("tracking Viewmodel", "Active Run received: ${run.toString()}")
        }
        .map { run ->
        if (run != null) {
            TrackingUiState(
                startTime = convertTimeUseCase(run.startTime),
                timerValue = convertTimeUseCase.timerFormat(run.elapsedTime),
                currentDistance = run.currentDistance.toString(),
                currentPace = paceCalcUseCase(run.currentDistance, run.elapsedTime).toString(),
                route = run.route
            )
        } else {
            TrackingUiState()
        }
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = TrackingUiState()
        )

    fun startRun() {
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.Companion.ACTION_START_RUN
        }
        ContextCompat.startForegroundService(context, intent)
        Log.d("tracking Viewmodel", "Location service Started from viewmodel")
    }

    fun stopRun() {
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.Companion.ACTION_STOP_RUN
        }
        context.startService(intent)
        Log.d("tracking Viewmodel", "Location service stopped from viewmodel")
    }
}