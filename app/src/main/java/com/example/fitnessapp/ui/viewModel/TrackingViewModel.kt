package com.example.fitnessapp.ui.viewModel

import androidx.lifecycle.ViewModel
import com.example.fitnessapp.Data.Repositories.RunRepoImpl
import com.example.fitnessapp.Domain.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val runRepo : RunRepository
) : ViewModel() {
    // this collects the activeRun stateFlow from the repo and translates it into TrackingUiState and exposes it to the UI
}