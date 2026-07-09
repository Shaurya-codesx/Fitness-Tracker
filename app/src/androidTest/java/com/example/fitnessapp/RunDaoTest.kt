package com.example.fitnessapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnessapp.Data.Model.AppDatabase
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.runDAO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RunDaoTest {

    private lateinit var database: AppDatabase // Replace with your DB class name
    private lateinit var dao: runDAO

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // CRITICAL: We use an In-Memory database.
        // This means it exists only in RAM and is wiped the second the test finishes.
        // It will NOT delete your real app data on the emulator!
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Allowed ONLY in tests
            .build()
        dao = database.getDAO()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun dao_test() = runTest {
        // GIVEN: A mock run object
        val mockRun = RunEntity(
            id = 1,
            startTime = 1000L,
            endTime = 2000L,
            distanceInMeters = 5000f,
            stepsTaken = 6000,
            caloriesBurned = 350f,
            route = emptyList(),
            isSynced = false
        )

        // WHEN: We insert the run into the database
        dao.insertRun(mockRun)

        // THEN: We read the database and assert the run is there
        val allRuns = dao.getAllRuns().first() // .first() collects the first Flow emission

        assertTrue(allRuns.isNotEmpty())
        assertEquals(5000f, allRuns[0].distanceInMeters)
        assertEquals(1, allRuns[0].id)
    }

    @Test
    fun dao_deleteRunTest() = runTest {
        val mockRun = RunEntity(
            id = 1,
            startTime = 1000L,
            endTime = 2000L,
            distanceInMeters = 5000f,
            stepsTaken = 6000,
            caloriesBurned = 350f,
            route = emptyList(),
            isSynced = false
        )

        // WHEN
        dao.insertRun(mockRun)
        dao.deleteRuns()

        // THEN
        val allRuns = dao.getAllRuns().first()
        assertTrue(allRuns.isEmpty())
    }
}