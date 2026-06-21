package com.example.fitnessapp.ui.activity.Stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.Domain.UseCases.GetRunRangeUseCase
import com.example.fitnessapp.ui.UiStates.StatsData
import com.example.fitnessapp.ui.UiStates.StatsUIState
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val runRepo : RunRepository,
    private val timeFormat : ConvertTimeUseCase,
    private val getRange : GetRunRangeUseCase
) : ViewModel() {

    val selectedFilter = MutableStateFlow(RunFilter.DAY)
     fun onFilterSelected(filter : RunFilter) {
        selectedFilter.value = filter
    }

     val statsUIState : StateFlow<StatsUIState> = selectedFilter
        .flatMapLatest { filter ->
            val(startTime, endTime) = getRange(filter)
            runRepo.analyticsDataInRange(startTime, endTime)
                .map { data ->
                    val uiData = StatsData(
                        totalDistance = "%.1f KM".format(data.totalDistance / 1000f),
                        totalCalories = "%.1f CAL".format(data.totalCalories),
                        totalSteps = data.totalSteps.toString(),
                        totalDuration = timeFormat.formatDurationShort(data.totalDuration),
                        totalRuns = data.totalRuns.toString()
                    )
                    StatsUIState.Success(uiData) as StatsUIState
                }
                .onStart { emit(StatsUIState.Loading) }
                .catch { e -> emit(StatsUIState.Error(e.message ?: "Unknown Error")) }
                }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsUIState.Loading
        )
}