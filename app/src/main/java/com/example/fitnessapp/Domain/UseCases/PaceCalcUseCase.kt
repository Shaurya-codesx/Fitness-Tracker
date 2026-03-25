package com.example.fitnessapp.Domain.UseCases

import javax.inject.Inject
import kotlin.math.round

class PaceCalcUseCase @Inject constructor() {
    operator fun invoke (distance : Float, time : Long) : Float {
        if (time <= 0L || distance <= 0f) {
            return 0.0f
        }
        val timeInSeconds = time / 1000f
        val paceInMetersPerSecond = distance / timeInSeconds
        return round(paceInMetersPerSecond * 10) / 10f
    }
}