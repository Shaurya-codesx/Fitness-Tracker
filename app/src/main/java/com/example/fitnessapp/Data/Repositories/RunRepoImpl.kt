package com.example.fitnessapp.Data.Repositories

import android.widget.Toast
import com.example.fitnessapp.Data.Model.Entities.ActiveRun
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.Domain.CalcDistanceUseCase
import com.example.fitnessapp.Domain.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RunRepoImpl(
    private val runDAO : runDAO,
    private val calcDistanceUseCase : CalcDistanceUseCase
) : RunRepository{

    private val _activeRun = MutableStateFlow<ActiveRun?>(null) // so this private variable is mutable for us to change it, and the public is read only
    override val activeRun : StateFlow<ActiveRun?> = _activeRun // DOUBT HERE

    override fun startRun() {
        if (_activeRun.value != null) {
            // If there is an active run, do nothing
            return
        }
        else {
            _activeRun.value = ActiveRun (
                startTime = System.currentTimeMillis(),
                elapsedTime = 0,
                currentDistance = 0.0,
                route = emptyList(),
                trackingStatus = true
            )
        }
    }

    override fun stopRun() {
        // stop the timer

    }

    override fun getAllRuns(): Flow<List<RunEntity>> {
        return runDAO.getAllRuns()
    }

    override fun addLocationPoint(point: LocationPoints) {
        val current = _activeRun.value ?: return // does nothing if activeRun object is null
        val incrementalDistance = if (current.route.isNotEmpty()) {
            calcDistanceUseCase(current.route.last(), point)
        } else 0.0

        val updatedRoute = current.route + point


        // finds the incremental distance value and then adds it to the current distance of the activeRun,
        // also adds the last GeoPoint to the route list
        _activeRun.value = current.copy(
            currentDistance = current.currentDistance + incrementalDistance,
            route = updatedRoute
        )
    }
}