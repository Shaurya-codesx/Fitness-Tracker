package com.example.fitnessapp.Data.Model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnessapp.Data.Model.Entities.RunEntity
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
}