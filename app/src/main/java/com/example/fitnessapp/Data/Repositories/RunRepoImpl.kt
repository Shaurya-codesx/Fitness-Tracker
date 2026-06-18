package com.example.fitnessapp.Data.Repositories

import android.util.Log
import com.example.fitnessapp.Data.Model.DistAndDuration
import com.example.fitnessapp.Data.Model.Entities.ActiveRun
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.WeeklyDistances
import com.example.fitnessapp.Data.Model.analyticsData
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.Domain.LocationDataSource
import com.example.fitnessapp.Domain.UseCases.CalcDistanceUseCase
import com.example.fitnessapp.Domain.RunRepository
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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepoImpl @Inject constructor(
    private val runDAO : runDAO,
) : RunRepository{

    override fun getAllRuns(): Flow<List<RunEntity>> {
        return runDAO.getAllRuns()
    }

    override fun getRunById(id : Long): Flow<RunEntity> {
        return runDAO.getRunById(id)
    }

    override fun getDistAndDurationInRange(startTime: Long, endTime: Long): Flow<DistAndDuration> {
        return runDAO.getDistAndDurationInRange(startTime, endTime)
    }

    override fun getNoOfRuns(
        startTime: Long,
        endTime: Long
    ): Flow<Int> {
        return runDAO.getNoOfRuns(startTime, endTime)
    }

    override fun getWeeklyDistances(startTime: Long, endTime: Long): Flow<List<WeeklyDistances>> {
        return runDAO.getWeeklyDistances(startTime, endTime)
    }

    override fun getRunsInRange(startTime: Long, endTime: Long): Flow<List<RunEntity>> {
        return runDAO.getRunsInRange(startTime, endTime)
    }

    override fun analyticsDataInRange(startTime: Long, endTime: Long): Flow<analyticsData> {
        return runDAO.analyticsDataInRange(startTime, endTime)
    }
}