package com.example.fitnessapp.Data.StepCounter

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.time.delay
import javax.inject.Inject

class MockStepFlow @Inject constructor() : StepTracker {
    override fun getSteps(): Flow<Int> = callbackFlow{
        var currentSteps = 0

        // This loop will run as long as the flow is being collected
        while (true) {
            // Simulate a step every 400ms to 600ms (a typical jogging cadence)
            val randomStrideDelay = (400..600).random().toLong()
            delay(randomStrideDelay)

            currentSteps += 1
            trySend(currentSteps)
        }
    }
}