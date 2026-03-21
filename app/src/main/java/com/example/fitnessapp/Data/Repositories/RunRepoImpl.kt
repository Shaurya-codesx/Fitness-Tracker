package com.example.fitnessapp.Data.Repositories

import android.util.Log
import com.example.fitnessapp.Data.Model.Entities.ActiveRun
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.Domain.LocationDataSource
import com.example.fitnessapp.Domain.UseCases.CalcDistanceUseCase
import com.example.fitnessapp.Domain.RunRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepoImpl @Inject constructor(
    private val runDAO : runDAO,
    private val calcDistanceUseCase : CalcDistanceUseCase,
    private val androidLocationProvider: LocationDataSource
) : RunRepository{

    private val _activeRun = MutableStateFlow<ActiveRun?>(null) // so this private variable is mutable for us to change it, and the public is read only
    override val activeRun : StateFlow<ActiveRun?> = _activeRun


    // a coroutine Scope is simply like a container that contains all the coroutines in that scope,it simply manages when can a coroutine start, cancelled, keeps track
    // every coroutine belongs to a scope
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // a job simply means a running coroutine task, Dispatcher means the thread on which the coroutine can run         Dispatchers.Main → UI thread
    //                                                                                                                 Dispatchers.IO → database / network
    //                                                                                                                 Dispatchers.Default → CPU work
    private var timerJob : Job? = null
    private var locationCollectionJob : Job? = null

    override fun startRun() {
        if (_activeRun.value != null) {
            // If there is an active run, do nothing
            return
        }
        _activeRun.value = ActiveRun (
            startTime = System.currentTimeMillis(),
            elapsedTime = 0,
            currentDistance = 0f,
            route = emptyList(),
            trackingStatus = true
        )
        timerJob?.cancel()
        timerJob = repositoryScope.launch {
            while (_activeRun.value!= null) {
                delay(1000)
                val current = _activeRun.value ?: break
                val updatedTime = (System.currentTimeMillis() - current.startTime) / 1000
                _activeRun.value = current.copy(elapsedTime = updatedTime)
                Log.d("timerCheck", "elapsed time : ${_activeRun.value?.elapsedTime}")
            }
        }

        locationCollectionJob?.cancel()
        locationCollectionJob = repositoryScope.launch {
            Log.d("lokation", "Location tracking started")
            androidLocationProvider.locationDataStream.collect { points ->
                addLocationPoint(points)
            }
        }
    }

    override suspend fun stopRun() { // here we make the stopRun function suspend and let the caller decide the scope of the coroutine
        timerJob?.cancel()
        timerJob = null

        locationCollectionJob?.cancel()
        locationCollectionJob = null

        val finalRun = _activeRun.value ?: return

        val timeInSeconds = finalRun.elapsedTime / 1000f
        val avgPaceMps = if (timeInSeconds > 0) {
            finalRun.currentDistance / timeInSeconds
        } else 0f

        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val readableStartTime = sdf.format(java.util.Date(finalRun.startTime))
        val readableEndTime = sdf.format(java.util.Date(finalRun.startTime + finalRun.elapsedTime))


        val finalRunEntity = RunEntity(
            startTime = finalRun.startTime,
            endTime = finalRun.startTime + finalRun.elapsedTime,
            distanceInMeters = finalRun.currentDistance,
            avgPace = avgPaceMps,
            route = finalRun.route,
            isSynced = false
        )
        runDAO.insertRun(finalRunEntity)
        _activeRun.value = null
    }

    override fun getAllRuns(): Flow<List<RunEntity>> {
        return runDAO.getAllRuns()
    }


    override fun addLocationPoint(point: LocationPoints) {
        val current = _activeRun.value ?: return // does nothing if activeRun object is null
        val incrementalDistance = if (current.route.isNotEmpty()) {
            calcDistanceUseCase(current.route.last(), point)
        } else 0f

        val updatedRoute = current.route + point

        // finds the incremental distance value and then adds it to the current distance of the activeRun,
        // also adds the last GeoPoint to the route list
        _activeRun.value = current.copy(
            currentDistance = current.currentDistance + incrementalDistance,
            route = updatedRoute
        )
        Log.d("distCheck", "distance : ${_activeRun.value?.currentDistance}")
    }
}