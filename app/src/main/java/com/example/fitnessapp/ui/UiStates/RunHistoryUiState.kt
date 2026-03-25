package com.example.fitnessapp.ui.UiStates

import com.example.fitnessapp.ui.UiStates.RunUiModel

data class RunHistoryUiState (
    val runs : List<RunUiModel> = emptyList()
)