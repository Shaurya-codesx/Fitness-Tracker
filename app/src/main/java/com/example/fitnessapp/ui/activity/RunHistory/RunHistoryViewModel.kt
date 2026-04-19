package com.example.fitnessapp.ui.activity.RunHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.Domain.UseCases.GetRunRangeUseCase
import com.example.fitnessapp.Domain.UseCases.PaceCalcUseCase
import com.example.fitnessapp.ui.UiStates.RunHistoryUiState
import com.example.fitnessapp.ui.UiStates.RunUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RunHistoryViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val convertTimeUseCase: ConvertTimeUseCase,
    private val paceCalcUseCase: PaceCalcUseCase,
    private val getRange : GetRunRangeUseCase
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(RunFilter.ALL)
    fun onFilterSelected(filter: RunFilter) { // this function changes the value of the selected filter when a new filter is selected
        selectedFilter.value = filter
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val runHistoryUiState: StateFlow<RunHistoryUiState> =
        selectedFilter
            .flatMapLatest { filter -> // when ever the selectedFilter value changes from the ui, this block runs with the new filter value
                //
                val (startTime, endTime) = getRange(filter)
                runRepository.getRunsInRange(startTime, endTime)

                val(monthStart, monthEnd) = getRange(RunFilter.MONTH)

                // Creating the two data streams
                val runsFlow = runRepository.getRunsInRange(startTime, endTime)
                val statsFlow = runRepository.getDistAndDurationInRange(monthStart, monthEnd)

                // Combine them so they flow together into the next step
                runsFlow.combine(statsFlow) { runs, stats ->
                    Pair(runs, stats)
                }
            }
            .map { (runEntities, stats) ->
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
                            distanceInMeters = "%.2f".format(runEntity.distanceInMeters / 1000),
                            avgPace = avgPace
                        )
                    },
                    totalDistance = "%.2f km".format((stats?.totalDistance ?: 0f) / 1000f),
                    totalTime = convertTimeUseCase.formatDurationShort(stats?.totalDuration ?: 0L)
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                RunHistoryUiState()
            )
}

