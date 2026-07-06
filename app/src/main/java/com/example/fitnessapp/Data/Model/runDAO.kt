package com.example.fitnessapp.Data.Model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.StatsDataClasses.AvgPaceModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.EnergyModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.RawRunModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.StepsModel
import com.example.fitnessapp.Data.Model.StatsDataClasses.analyticsData
import kotlinx.coroutines.flow.Flow

@Dao
interface runDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run : RunEntity)

    // Adds a whole list of runs at once (Used for restoring history)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuns(runs: List<RunEntity>)

    @Query("DELETE FROM runs")
    suspend fun deleteRuns()

    @Query("SELECT MIN(startTime) FROM runs")
    suspend fun getOldestRunTimestamp(): Long?

    @Query("SELECT * FROM runs WHERE isSynced = 0")
    suspend fun getUnsyncedRuns(): List<RunEntity>

    @Update
    suspend fun updateRuns(runs: List<RunEntity>)

    @Query("SELECT * FROM runs ORDER BY startTime DESC")
    fun getAllRuns() : Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE id = :id")
    fun getRunById(id : Long) : Flow<RunEntity>

    @Query("SELECT * FROM runs ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestRun(): RunEntity?

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
            -- When viewing a WEEK or a MONTH, group the bars by DAY (e.g., '2026-07-03')
            WHEN 'WEEK' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            WHEN 'MONTH' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            
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
        CASE :filter
            WHEN 'WEEK' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            WHEN 'MONTH' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            WHEN 'YEAR' THEN strftime('%Y-%m', startTime / 1000, 'unixepoch', 'localtime')
        END AS periodLabel,
        SUM(distanceinmeters) AS totalDistance 
    FROM runs
    WHERE startTime >= :startTime AND startTime <= :endTime
    GROUP BY periodLabel
    ORDER BY startTime ASC
""")
    fun getDistanceAnalytics(
        startTime: Long,
        endTime: Long,
        filter: String
    ): Flow<List<DistanceModel>>

    @Query("""
        SELECT distanceinmeters 
        FROM runs
        WHERE startTime >= :startTime AND startTime <= :endTime
    """)
    fun getRawDistancesForRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<Float>>

    @Query("""
    SELECT 
        CASE :filter
            WHEN 'WEEK' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            WHEN 'MONTH' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            WHEN 'YEAR' THEN strftime('%Y-%m', startTime / 1000, 'unixepoch', 'localtime')
        END AS periodLabel,
        SUM(endTime - startTime) AS totalDuration, 
        SUM(distanceinmeters) AS totalDistance
    FROM runs
    WHERE startTime >= :startTime AND startTime <= :endTime
    GROUP BY periodLabel
    ORDER BY startTime ASC
""")
    fun getPaceAnalytics(
        startTime: Long,
        endTime: Long,
        filter: String
    ): Flow<List<AvgPaceModel>>

    @Query("""
    SELECT 
        (endTime - startTime) AS durationMillis,
        distanceinmeters AS distanceMeters
    FROM runs
    WHERE startTime >= :startTime AND startTime <= :endTime
""")
    fun getRawRunsForRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<RawRunModel>>


    @Query("""
    SELECT 
        CASE :filter
            WHEN 'WEEK' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            WHEN 'MONTH' THEN strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime')
            WHEN 'YEAR' THEN strftime('%Y-%m', startTime / 1000, 'unixepoch', 'localtime')
        END AS periodLabel,
        SUM(caloriesBurned) AS totalCalories
    FROM runs
    WHERE startTime >= :startTime AND startTime <= :endTime
    GROUP BY periodLabel
    ORDER BY startTime ASC
""")
    fun getEnergyAnalytics(
        startTime: Long,
        endTime: Long,
        filter: String
    ): Flow<List<EnergyModel>>

    // 2. Raw query for the Intensity Donut Chart
    @Query("""
        SELECT caloriesBurned 
        FROM runs
        WHERE startTime >= :startTime AND startTime <= :endTime
    """)
    fun getRawCaloriesForRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<Float>>

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

    // ─── PERSONAL BEST QUERIES ───────────────────────────────────────────────

    // 1. Farthest Distance ever run (in meters)
    @Query("SELECT MAX(distanceinmeters) FROM runs")
    fun getRecordDistance(): Flow<Float?>

    // 2. Longest Duration ever run (in milliseconds)
    @Query("SELECT MAX(endTime - startTime) FROM runs")
    fun getRecordDuration(): Flow<Long?>

    // 3. Highest Calories burned in a single run
    @Query("SELECT MAX(caloriesBurned) FROM runs")
    fun getRecordCalories(): Flow<Float?>

    // 4. Most Steps taken in a single run
    @Query("SELECT MAX(stepsTaken) FROM runs")
    fun getRecordSteps(): Flow<Int?>

    // 5. Fastest Pace (lowest ratio of time to distance)
    // We strictly filter for runs >= 1000 meters to ensure it is a valid run.
    // We return the raw duration and distance to calculate the exact pace format in Kotlin.
    @Query("""
        SELECT 
            (endTime - startTime) AS durationMillis,
            distanceinmeters AS distanceMeters
        FROM runs
        WHERE distanceinmeters >= 1000
        ORDER BY (CAST((endTime - startTime) AS REAL) / distanceinmeters) ASC
        LIMIT 1
    """)
    fun getRecordPaceRun(): Flow<RawRunModel?>
    // ^ Note: We are reusing the RawRunModel you already created for the Pace donut chart!


    // For Home Screen
    @Query("""
        SELECT 
            SUM(distanceinmeters) as totalDistanceMeters,
            SUM(stepsTaken) as totalSteps,
            SUM(caloriesBurned) as totalCalories
        FROM runs
        WHERE startTime >= :startOfDay AND startTime <= :endOfDay
    """)
    fun getTodayStats(startOfDay: Long, endOfDay: Long): Flow<TodayStats?>

    // 3. Fetch all run start times for the Streak and Heatmap calculations
    // Ordering by DESC so the most recent runs are at the top of the list
    @Query("SELECT startTime FROM runs ORDER BY startTime DESC")
    fun getAllRunStartTimes(): Flow<List<Long>>
}