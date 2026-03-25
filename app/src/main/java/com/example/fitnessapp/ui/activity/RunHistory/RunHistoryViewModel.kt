package com.example.fitnessapp.ui.activity.RunHistory

import androidx.lifecycle.ViewModel
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.security.PrivateKey
import javax.inject.Inject

@HiltViewModel
class RunHistoryViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val convertTimeUseCase: ConvertTimeUseCase
) : ViewModel() {
    val runHistoryUiState : Flow<RunHistoryUiState> = runRepository.getAllRuns().map { runEntities ->
        RunHistoryUiState (
            runs = runEntities.map { runEntity ->
                RunUiModel(
                    id = runEntity.id,
                    startTime = convertTimeUseCase(runEntity.startTime),
                    endTime = convertTimeUseCase(runEntity.endTime),
                    duration = convertTimeUseCase(runEntity.endTime - runEntity.startTime),
                    distanceInMeters = runEntity.distanceInMeters.toString(),
                    avgPace = runEntity.avgPace.toString(),
                    route = runEntity.route
                )
            }
        )
    }
}