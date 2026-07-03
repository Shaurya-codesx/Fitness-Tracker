package com.example.fitnessapp.ui.activity.Tracking

import com.google.android.gms.common.api.ResolvableApiException

sealed class TrackingUiEvent {
    data class RequestEnableLocation(val exception: ResolvableApiException) : TrackingUiEvent()
    object ShowLocationError : TrackingUiEvent()
    object StartRunService : TrackingUiEvent()
    object LocationRestored : TrackingUiEvent()
    object ShowNoMovementDialogue : TrackingUiEvent()
}