package com.example.fitnessapp.Domain

import com.example.fitnessapp.Data.Model.DistAndDuration
import com.example.fitnessapp.Data.Model.Entities.ActiveRun
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.StepsModel
import com.example.fitnessapp.Data.Model.WeeklyDistances
import com.example.fitnessapp.Data.Model.StatsDataClasses.analyticsData
import com.example.fitnessapp.Domain.Wrapper.Resource
import com.example.fitnessapp.ui.activity.RunHistory.RunEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RunRepository { // this repository holds the data for the active run session and exposes it to viewModel
    // this is a repository that states what all things can be done in activeRun session, no implementation

    fun getAllRuns() : Flow<List<RunEntity>>

    fun getRunsInRange(startTime: Long, endTime: Long) : Flow<List<RunEntity>>

    fun getRunById(id : Long) : Flow<RunEntity>

    fun getDistAndDurationInRange(startTime : Long, endTime : Long) : Flow<DistAndDuration>

    fun getNoOfRuns(startTime: Long, endTime: Long) : Flow<Int>

    fun getWeeklyDistances(startTime: Long, endTime: Long) : Flow<List<WeeklyDistances>>

    fun getStepsAnalytics(startTime: Long, endTime: Long, filter: String) : Flow<List<StepsModel>>

    fun getDistanceAnalytics(startTime: Long, endTime: Long, filter: String) : Flow<List<DistanceModel>>

    fun analyticsDataInRange(startTime: Long, endTime: Long) : Flow<analyticsData>
}