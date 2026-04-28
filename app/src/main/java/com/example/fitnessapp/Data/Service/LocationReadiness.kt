package com.example.fitnessapp.Data.Service

import com.google.android.gms.common.api.ResolvableApiException

sealed class LocationReadiness {
    object Ready : LocationReadiness()
    data class Resolvable(val exception: ResolvableApiException) : LocationReadiness()
    object NotResolvable : LocationReadiness()
}