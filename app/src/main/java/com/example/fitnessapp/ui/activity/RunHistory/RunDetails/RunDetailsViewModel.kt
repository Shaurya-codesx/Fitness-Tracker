package com.example.fitnessapp.ui.activity.RunHistory.RunDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.Domain.RunRepository
import com.example.fitnessapp.Domain.UseCases.ConvertTimeUseCase
import com.example.fitnessapp.Domain.UseCases.PaceCalcUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RunDetailsViewModel @Inject constructor(
    private val runRepository: RunRepository,
    private val convertTimeUseCase: ConvertTimeUseCase,
    private val paceCalcUseCase: PaceCalcUseCase,
    savedStateHandle: SavedStateHandle // this savedState is like a backpack that the view Model carries
    // it holds data that is passed when moving from one screen to another and it is remembered so it survives config changes as well as app kill
) : ViewModel(){
    private val runId: Long = checkNotNull(savedStateHandle["runId"]) // this line catches the Id from the NavHost
    // the Navigation component automatically puts that {runId} value into the SavedStateHandle bag which was clicked

    // instead of manually passing a variable id from the ui to the view model, the view model just simply looks in its bag for something named "runID"
    // which triggers the repo function call

    // we have different states also which means UI simply doesn't react to the flow of data it reacts to the states
    // if there is an error from loading the data ui shows error state, if it is taking time to update the flow it shows loading state
    // and on success state with the data


    val runDetailsState: StateFlow<RunDetailsState> =
        runRepository.getRunById(runId)
            .map { run ->
                if (run == null) {
                    return@map RunDetailsState.Error
                }

                val duration = run.endTime - run.startTime
                val avgPace = paceCalcUseCase(run.distanceInMeters, duration)

                val uiState = RunDetailsUiState(
                    runId = run.id,
                    startTime = convertTimeUseCase(run.startTime),
                    endTime = convertTimeUseCase(run.endTime),
                    duration = convertTimeUseCase.timerFormat(duration),
                    distance = "%.2f".format(run.distanceInMeters/1000) ,
                    avgPace = avgPace,
                    routeList = run.route
                )

                RunDetailsState.Success(uiState)
            }
            .onStart {
                emit(RunDetailsState.Loading)
            }
            .catch {
                emit(RunDetailsState.Error)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                RunDetailsState.Loading
            )
}