package com.example.fitnessapp.Domain.UseCases

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitnessapp.Data.Model.WeeklyDistances
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.text.format
import kotlin.time.ExperimentalTime

class WeeklyDistanceHelper @Inject constructor() {
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalTime::class)
    operator fun invoke(
        dbList: List<WeeklyDistances>,
        startTimeMillis: Long,
        endTimeMillis: Long
    ) : Flow<List<WeeklyDistances>> = flow { // this converts the entire function to a flow
        // Use java.time.Instant (Standard Java 8+)
        val startDate = Instant.ofEpochMilli(startTimeMillis)
            .atZone(ZoneId.systemDefault()).toLocalDate()

        val endDate = Instant.ofEpochMilli(endTimeMillis)
            .atZone(ZoneId.systemDefault()).toLocalDate()

        // Map the DB results into a Map for quick lookup
        val dbMap = dbList.associateBy({ it.day }, { it.totalDistance })

        val fullList = mutableListOf<WeeklyDistances>()

        // Calculate total days to iterate
        val daysInRange = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1))

        var currentPoint = startDate
        for (i in 0 until daysInRange) {
            // Format current date to match the String keys in dbMap
            val dateString = currentPoint.format(formatter)

            val distance = dbMap[dateString] ?: 0.0f

            fullList.add(WeeklyDistances(day = dateString, totalDistance = distance))

            // Move to next day
            currentPoint = currentPoint.plusDays(1)
        }

        emit(fullList) // this emits the flow
    }
}