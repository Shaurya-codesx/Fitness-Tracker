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

    @Query("SELECT * FROM runs ORDER BY startTime DESC")
    fun getALLRunsSortByDate () : Flow<List<RunEntity>>
}