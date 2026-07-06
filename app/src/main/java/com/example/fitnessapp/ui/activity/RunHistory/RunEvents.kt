package com.example.fitnessapp.ui.activity.RunHistory


sealed class RunEvents {
    object LocationDisabled : RunEvents()
    object LocationRestored : RunEvents()
    object NoMovement : RunEvents()
}