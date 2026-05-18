package com.example.fitnessapp.ui.activity.Stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.Domain.UseCases.GetRunRangeUseCase
import com.example.fitnessapp.Domain.UseCases.PaceCalcUseCase
import com.example.fitnessapp.ui.UiStates.StatsData
import com.example.fitnessapp.ui.UiStates.StatsUIState
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val runRepo : RunRepository,
    private val getRange : GetRunRangeUseCase,
    private val paceCalcUseCase: PaceCalcUseCase,
    private val convertTimeUseCase: ConvertTimeUseCase
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(RunFilter.WEEK)
     fun onFilterSelected(filter : RunFilter) {
        selectedFilter.value = filter
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val statsUiState : StateFlow<StatsUIState> = selectedFilter
        .flatMapLatest { filter ->
            val (startTime, endTime) = getRange(filter)

            val statsFlow = runRepo.getDistAndDurationInRange(startTime, endTime)
            val totalRuns = runRepo.getNoOfRuns(startTime, endTime)

            statsFlow.combine(totalRuns) {stats, totalRuns ->
                Pair(stats, totalRuns)
            }
        }
        .map { (stats, totalRuns) ->
            val avgPace = paceCalcUseCase(stats.totalDistance, stats.totalDuration)
            val result : StatsUIState = StatsUIState.Success(
                StatsData(
                    totalDistance = "%.2f km".format((stats.totalDistance ?: 0f) / 1000f),
                    totalTime = convertTimeUseCase.formatDurationShort(stats?.totalDuration ?: 0L),
                    totalAvgPace = avgPace,
                    totalRuns = totalRuns.toString()
                )
            )
            result
        }
        .catch { e ->
            // FIX: Added the 'e ->' parameter so catch functions correctly
            emit(StatsUIState.Error(e.message ?: "Something Went Wrong"))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            StatsUIState.Loading
        )
}