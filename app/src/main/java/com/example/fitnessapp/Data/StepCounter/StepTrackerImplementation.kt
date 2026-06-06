package com.example.fitnessapp.Data.StepCounter

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class StepTrackerImplementation @Inject constructor(
    private val sensorManager: SensorManager
) : StepTracker{
    override fun getSteps(): Flow<Int> = callbackFlow {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var initialSteps = -1

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.firstOrNull()?.let { totalStepsSinceReboot ->
                    // Capture the baseline on the very first event of this session
                    if (initialSteps == -1) {
                        initialSteps = totalStepsSinceReboot.toInt()
                    }

                    // Emit only the steps taken during this active session
                    val sessionSteps = totalStepsSinceReboot.toInt() - initialSteps
                    trySend(sessionSteps)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed for step counter
            }
        }

        stepSensor?.let {
            sensorManager.registerListener(
                listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // Clean up when the flow is cancelled
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

}