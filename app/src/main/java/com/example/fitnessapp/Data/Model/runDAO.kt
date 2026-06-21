package com.example.fitnessapp.Data.Model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import kotlinx.coroutines.flow.Flow

@Dao
interface runDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run : RunEntity)

    @Query("DELETE FROM runs")
    suspend fun deleteRun()

    @Query("SELECT * FROM runs ORDER BY startTime DESC")
    fun getAllRuns() : Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :id")
    fun getRunById(id : Long) : Flow<RunEntity>

    @Query("SELECT * FROM runs WHERE startTime BETWEEN :startTime AND :endTime ORDER BY startTime DESC")
    fun getRunsInRange(startTime : Long, endTime : Long) : Flow<List<RunEntity>>

    @Query("SELECT \n" +
            "    COALESCE(SUM(distanceInMeters), 0) AS totalDistance,\n" +
            "    COALESCE(SUM(endTime - startTime), 0) AS totalDuration\n" +
            "FROM runs\n" +
            "WHERE startTime BETWEEN :startTime AND :endTime")
    fun getDistAndDurationInRange (startTime : Long, endTime : Long) : Flow<DistAndDuration> //COALESCE replaces the null values with default ones

    @Query("Select count(*) from runs where startTime BETWEEN :startTime AND :endTime")
    fun getNoOfRuns (startTime: Long, endTime: Long) : Flow<Int>

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime') AS day, 
            SUM(distanceInMeters) AS totalDistance 
        FROM runs 
        WHERE startTime BETWEEN :startTime AND :endTime 
        GROUP BY day 
        ORDER BY day ASC
    """)
    fun getWeeklyDistances(startTime: Long, endTime: Long): Flow<List<WeeklyDistances>>

    @Query("""
        SELECT 
            CASE :filter
                -- When viewing a WEEK, group the bars by DAY (e.g., '2026-06-21')
                WHEN 'WEEK' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
                
                -- When viewing a MONTH, group the bars by WEEK OF THE YEAR (e.g., '2026-24')
                WHEN 'MONTH' THEN strftime('%Y-%W', startTime / 1000, 'unixepoch', 'localtime')
                
                -- When viewing a YEAR, group the bars by MONTH (e.g., '2026-06')
                WHEN 'YEAR' THEN strftime('%Y-%m', startTime / 1000, 'unixepoch', 'localtime')
            END AS periodLabel,
            SUM(stepsTaken) AS stepCount
        FROM runs
        WHERE startTime >= :startTime AND startTime <= :endTime
        GROUP BY periodLabel
        ORDER BY startTime ASC
    """)
    fun getStepsAnalytics(startTime: Long, endTime: Long, filter: String) : Flow<List<StepsModel>>

    @Query("""
    SELECT 
        COALESCE(SUM(distanceInMeters), 0.0) AS totalDistance, 
        COALESCE(SUM(stepsTaken), 0) AS totalSteps, 
        COALESCE(SUM(caloriesBurned), 0.0) AS totalCalories,
        COALESCE(SUM(endTime - startTime), 0) AS totalDuration,
        COUNT(*) AS totalRuns
    FROM runs 
    WHERE startTime BETWEEN :startTime AND :endTime
""")
    fun analyticsDataInRange(startTime: Long, endTime: Long) : Flow<analyticsData>
}