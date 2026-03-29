package com.example.fitnessapp.ui.activity.RunHistory.RunDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun RunDetailScreen(navController: NavController) {

    val runDetailsViewModel : RunDetailsViewModel = hiltViewModel()
    val uiState by runDetailsViewModel.runDetailsState.collectAsState()

    when(uiState) {
        is RunDetailsState.Loading -> {

        }
        is RunDetailsState.Error -> {
        }
        is RunDetailsState.Success -> {
            val data = (uiState as RunDetailsState.Success).data // understand this line first
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Text(text = "Distance: ${data.distance} m")
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Duration: ${data.duration}")
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Avg Pace: ${data.avgPace}")
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Start: ${data.startTime}")
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "End: ${data.endTime}")
            }
        }
    }
}