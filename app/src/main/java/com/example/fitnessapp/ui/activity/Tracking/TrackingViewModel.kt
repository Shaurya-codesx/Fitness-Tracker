package com.example.fitnessapp.ui.activity.Tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Data.Location.androidLocationProvider
import com.example.fitnessapp.Data.Service.LocationForegroundService
import com.example.fitnessapp.Data.Service.LocationReadiness
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.TrackingRunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.Domain.UseCases.PaceCalcUseCase
import com.example.fitnessapp.Domain.Wrapper.Resource
import com.example.fitnessapp.ui.UiStates.TrackingUiState
import com.example.fitnessapp.ui.activity.RunHistory.RunEvents
import com.example.runtracker.ui.screens.TrackingScreen
import com.google.android.gms.common.api.ResolvableApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
//    private val runRepo : RunRepository,
    private val trackingRepo : TrackingRunRepository,
    private val convertTimeUseCase: ConvertTimeUseCase,
    private val paceCalcUseCase: PaceCalcUseCase,
    private val locationProvider : androidLocationProvider,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // this collects the activeRun stateFlow from the repo and translates it into TrackingUiState and exposes it to the UI
    // but activeRun is already stateFlow, but that is a domain model, we need a UI only model, with modified values
    // so this viewModel takes the domain model and converts it into presentation model and exposes it to the UI



    private val _uiEvent = MutableSharedFlow<TrackingUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    init {
        observeRunEvents()
    }


    // it takes in the cold StateFlow from the repo and converts it into a hot StateFlow that is exposed to the UI
    val trackingUiState: StateFlow<TrackingUiState> = trackingRepo.activeRun
        .map { run ->
        if (run != null) {
            TrackingUiState(
                startTime = convertTimeUseCase(run.startTime),
                timerValue = convertTimeUseCase.timerFormat(run.elapsedTime),
                currentDistance = "%.2f".format(run.currentDistance/1000),
                currentSteps = run.currentSteps.toString(),
                currentPace = paceCalcUseCase(run.currentDistance, run.elapsedTime),
                route = run.route
            )
        } else {
            TrackingUiState()
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = TrackingUiState()
        )

    fun startRun() {

        locationProvider.checkLocationSettings { readiness ->
            viewModelScope.launch {
                when(readiness) { //
                    is LocationReadiness.Ready -> {
                        _uiEvent.emit(TrackingUiEvent.StartRunService)
                    }
                    is LocationReadiness.Resolvable -> {
                        _uiEvent.emit(TrackingUiEvent.RequestEnableLocation(readiness.exception))
                    }
                    is LocationReadiness.NotResolvable -> {
                        _uiEvent.emit(TrackingUiEvent.ShowLocationError)
                    }
                }
            }
        }
    }


    private fun observeRunEvents() {
        viewModelScope.launch {
            trackingRepo.runEvents.collect { event ->
                when(event) {
                    is RunEvents.LocationDisabled -> {
                        _uiEvent.emit(TrackingUiEvent.ShowLocationError)
                    }
                    is RunEvents.NoMovement -> {
                        Log.d("runEvent", "No movement detected log")
                        _uiEvent.emit(TrackingUiEvent.ShowNoMovementDialogue)
                    }
                }
            }
        }
    }

    fun startIntent() {
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.Companion.ACTION_START_RUN
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopRun() {

        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.Companion.ACTION_STOP_RUN
        }
        context.startService(intent)
    }
}