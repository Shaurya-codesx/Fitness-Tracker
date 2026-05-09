package com.example.fitnessapp.ui.activity.RunHistory

import com.example.fitnessapp.Domain.Wrapper.Resource

sealed class RunEvents {
    object LocationDisabled : RunEvents()
    object NoMovement : RunEvents()
}