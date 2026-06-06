package com.example.fitnessapp.Data.StepCounter

import kotlinx.coroutines.flow.Flow

interface StepTracker {
    fun getSteps(): Flow<Int>
}