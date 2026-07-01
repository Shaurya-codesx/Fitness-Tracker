package com.example.fitnessapp.Data.Repositories

import android.util.Log
import com.example.fitnessapp.Data.Model.DistAndDuration
import com.example.fitnessapp.Data.Model.Entities.ActiveRun
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.StatsDataClasses.AvgPaceModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.EnergyModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.RawRunModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.StepsModel
import com.example.fitnessapp.Data.Model.WeeklyDistances
import com.example.fitnessapp.Data.Model.StatsDataClasses.analyticsData
import com.example.fitnessapp.Data.Model.TodayStats
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

    override suspend fun getUnsyncedRuns(): List<RunEntity> {
        return runDAO.getUnsyncedRuns()
    }

    override suspend fun updateRuns(runs: List<RunEntity>) {
        runDAO.updateRuns(runs)
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

    override fun getStepsAnalytics(
        startTime: Long,
        endTime: Long,
        filter: String
    ): Flow<List<StepsModel>> {
        return runDAO.getStepsAnalytics(startTime, endTime, filter)
    }

    override fun getDistanceAnalytics(
        startTime: Long,
        endTime: Long,
        filter: String
    ): Flow<List<DistanceModel>> {
        return runDAO.getDistanceAnalytics(startTime, endTime, filter)
    }

    override fun getRawDistancesForRange(startTime: Long, endTime: Long): Flow<List<Float>> {
        return runDAO.getRawDistancesForRange(startTime, endTime)
    }

    override fun getRunsInRange(startTime: Long, endTime: Long): Flow<List<RunEntity>> {
        return runDAO.getRunsInRange(startTime, endTime)
    }

    override fun getPaceAnalytics(
        startTime: Long,
        endTime: Long,
        filter: String
    ): Flow<List<AvgPaceModel>> {
        return runDAO.getPaceAnalytics(startTime, endTime, filter)
    }

    override fun getRawRunsForRange(startTime: Long, endTime: Long): Flow<List<RawRunModel>> {
        return runDAO.getRawRunsForRange(startTime, endTime)
    }

    override fun getEnergyAnalytics(
        startTime: Long,
        endTime: Long,
        filter: String
    ): Flow<List<EnergyModel>> {
        return runDAO.getEnergyAnalytics(startTime, endTime, filter)
    }

    override fun getRawCaloriesForRange(startTime: Long, endTime: Long): Flow<List<Float>> {
        return runDAO.getRawCaloriesForRange(startTime, endTime)
    }

    override fun analyticsDataInRange(startTime: Long, endTime: Long): Flow<analyticsData> {
        return runDAO.analyticsDataInRange(startTime, endTime)
    }

    // Personal Bests
    override fun getRecordDistance(): Flow<Float?> {
        return runDAO.getRecordDistance()
    }

    override fun getRecordDuration(): Flow<Long?> {
        return runDAO.getRecordDuration()
    }

    override fun getRecordCalories(): Flow<Float?> {
        return runDAO.getRecordCalories()
    }

    override fun getRecordSteps(): Flow<Int?> {
        return runDAO.getRecordSteps()
    }

    override fun getRecordPaceRun(): Flow<RawRunModel?> {
        return runDAO.getRecordPaceRun()
    }

    override fun getTodayStats(startOfDay: Long, endOfDay: Long): Flow<TodayStats?> {
        return runDAO.getTodayStats(startOfDay, endOfDay)
    }

    override fun getAllRunStartTimes(): Flow<List<Long>> {
        return runDAO.getAllRunStartTimes()
    }
}