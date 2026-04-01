package com.example.fitnessapp.ui.activity.RunHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.Domain.UseCases.PaceCalcUseCase
import com.example.fitnessapp.ui.UiStates.RunHistoryUiState
import com.example.fitnessapp.ui.UiStates.RunUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.security.PrivateKey
import javax.inject.Inject

@HiltViewModel
class RunHistoryViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val convertTimeUseCase: ConvertTimeUseCase,
    private val paceCalcUseCase: PaceCalcUseCase
) : ViewModel() {

    val runHistoryUiState : StateFlow<RunHistoryUiState> = runRepository.getAllRuns().map { runEntities ->
        // REMEMBER :------------------------ each RunEntity still has the route list which is heavy, fine for now, but may need to
        // write a better query than getALLRuns to only get times and distance, and another query to call routes of specific RunEntity
        // when need to show on the map
        RunHistoryUiState(
            runs = runEntities.map { runEntity ->
                val duration = runEntity.endTime - runEntity.startTime
                val avgPace = paceCalcUseCase(runEntity.distanceInMeters, duration)
                RunUiModel(
                    id = runEntity.id,
                    startTime = convertTimeUseCase(runEntity.startTime),
                    endTime = convertTimeUseCase(runEntity.endTime),
                    duration = convertTimeUseCase.timerFormat(duration),
                    distanceInMeters = "%.2f".format(runEntity.distanceInMeters/1000),
                    avgPace = avgPace
                )
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        RunHistoryUiState()
    )
}