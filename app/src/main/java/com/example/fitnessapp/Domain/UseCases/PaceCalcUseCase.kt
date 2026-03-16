package com.example.fitnessapp.Domain.UseCases

import javax.inject.Inject

class PaceCalcUseCase @Inject constructor() {
    operator fun invoke (distance : Float, time : Long) : Float {
        val timeInSeconds = time / 1000f
        return distance / timeInSeconds
    }
}