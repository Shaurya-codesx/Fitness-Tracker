package com.example.fitnessapp.Data.Repositories

import android.util.Log
import com.example.fitnessapp.Data.Model.Entities.ActiveRun
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.Data.StepCounter.StepTracker
import com.example.fitnessapp.Domain.LocationDataSource
import com.example.fitnessapp.Domain.TrackingRunRepository
import com.example.fitnessapp.Domain.UseCases.CalcDistanceUseCase
import com.example.fitnessapp.Domain.Wrapper.Resource
import com.example.fitnessapp.ui.activity.RunHistory.RunEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingRepoImpl @Inject constructor(
    private val runDAO : runDAO,
    private val calcDistanceUseCase : CalcDistanceUseCase,
    private val androidLocationProvider: LocationDataSource,
    private val stepTracker: StepTracker
) : TrackingRunRepository {

    private val _activeRun = MutableStateFlow<ActiveRun?>(null) // so this private variable is mutable for us to change it, and the public is read only
    override val activeRun : StateFlow<ActiveRun?> = _activeRun

    private val _runEvents = MutableSharedFlow<RunEvents>()
    override val runEvents : Flow<RunEvents> = _runEvents


    // a coroutine Scope is simply like a container that contains all the coroutines in that scope,it simply manages when can a coroutine start, cancelled, keeps track
    // every coroutine belongs to a scope
    // a job simply means a running coroutine task, Dispatcher means the thread on which the coroutine can run         Dispatchers.Main → UI thread
    //                                                                                                                 Dispatchers.IO → database / network
    //                                                                                                                 Dispatchers.Default → CPU work
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timerJob : Job? = null
    private var locationCollectionJob : Job? = null
    private var stepTrackerJob : Job? = null


    override fun startRun() {
        if (_activeRun.value != null) {
            // If there is an active run, do nothing
            return
        }
        _activeRun.value = ActiveRun (
            startTime = System.currentTimeMillis(),
            elapsedTime = 0,
            currentDistance = 0f,
            currentSteps = 0,
            route = emptyList(),
            trackingStatus = true
        )
        timerJob?.cancel()
        timerJob = repositoryScope.launch {
            while (_activeRun.value!= null) {
                delay(1000)
                val current = _activeRun.value ?: break
                val updatedTime = (System.currentTimeMillis() - current.startTime)
                _activeRun.value = current.copy(elapsedTime = updatedTime)
                Log.d("timerCheck", "elapsed time : ${_activeRun.value?.elapsedTime}")
            }
        }

        stepTrackerJob?.cancel()
        stepTrackerJob = repositoryScope.launch {
            stepTracker.getSteps().collect { steps ->
                _activeRun.update { currentRun->
                    currentRun?.copy(currentSteps = steps)
                }
                Log.d("StepsTracker", "current steps = $_activeRun.currentSteps")
            }
        }

        locationCollectionJob?.cancel()
        locationCollectionJob = repositoryScope.launch {
            Log.d("lokation", "Location tracking started")
            androidLocationProvider.locationDataStream.collect { point ->
                when(point) {
                    is Resource.Error -> {
                        Log.d("runEvent", "Location Disabled run event emitted")
                        _runEvents.emit(RunEvents.LocationDisabled)
                    }
                    is Resource.Success<*> -> {
                        addLocationPoint(point.data as LocationPoints)
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    override suspend fun stopRun() { // here we make the stopRun function suspend and let the caller decide the scope of the coroutine
        timerJob?.cancel()
        timerJob = null

        locationCollectionJob?.cancel()
        locationCollectionJob = null

        stepTrackerJob?.cancel()
        stepTrackerJob = null

        val finalRun = _activeRun.value ?: return



        if (finalRun.currentDistance > 0) {
            val finalRunEntity = RunEntity(
                startTime = finalRun.startTime,
                endTime = System.currentTimeMillis(),
                distanceInMeters = finalRun.currentDistance,
                stepsTaken = finalRun.currentSteps,
                route = finalRun.route,
                isSynced = false
            )
            runDAO.insertRun(finalRunEntity)
        } else {
            _runEvents.emit(RunEvents.NoMovement)
            Log.d("runEvent", "Run discarded - no movement run event")
        }
        _activeRun.value = null
    }

    override fun addLocationPoint(point: LocationPoints) {
        val current = _activeRun.value ?: return // does nothing if activeRun object is null
        val incrementalDistance = if (current.route.isNotEmpty()) {
            calcDistanceUseCase(current.route.last(), point)
        } else 0f


        // if incremental distance < minThreshold then we do not send the point forward, coz GPS data may be noisy so we filer here

        if (current.route.isNotEmpty() && incrementalDistance < 3f) {
            Log.d("lokation", "location point skipped by repo, inc distance < 3m")
            return
        }

        val updatedRoute = current.route + point

        // finds the incremental distance value and then adds it to the current distance of the activeRun,
        // also adds the last GeoPoint to the route list
        _activeRun.value = current.copy(
            currentDistance = current.currentDistance + incrementalDistance,
            route = updatedRoute
        )
        Log.d("lokation", "distance : ${_activeRun.value?.currentDistance}")

    }
}