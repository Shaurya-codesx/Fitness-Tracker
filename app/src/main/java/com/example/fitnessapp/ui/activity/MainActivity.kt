package com.example.fitnessapp.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.ui.UiStates.TrackingUiState
import com.example.fitnessapp.ui.activity.RunHistory.RunHistoryScreen
import com.example.fitnessapp.ui.activity.Tracking.OsmMapview
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.ui.activity.Tracking.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val trackingViewModel : TrackingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                val uiState by trackingViewModel.trackingUiState.collectAsStateWithLifecycle()
                Column(modifier = Modifier.fillMaxSize()) {
                    // Use weight(1f) to make them share the screen space equally
                    Box(modifier = Modifier.weight(1f)) {
                        Uitesting(uiState, trackingViewModel)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        RunHistoryScreen()
                    }
                }
            }
        }
    }
}


@Composable
fun Uitesting(uiState : TrackingUiState, trackingViewModel : TrackingViewModel) {
    val isRunStarted = uiState.startTime.isNotEmpty()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 2. If the run is started, show the Map in the background
        if (isRunStarted && uiState.route.isNotEmpty()) {
            OsmMapview(modifier = Modifier.fillMaxSize(), uiState.route)
            Log.d("uitesting", "Uitesting: last location : ${uiState.route.last().toString()}")
        }

        // 3. Your UI Overlay
        Box(
            modifier = Modifier
                .padding(16.dp)
                .shadow(15.dp, RoundedCornerShape(20.dp), spotColor = Color.Red)
                .then(if (isRunStarted) Modifier.align(Alignment.BottomCenter) else Modifier.fillMaxSize())
                .clip(RoundedCornerShape(20))
                .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Start Time : ${uiState.startTime}")
                Text(text = "Distance: ${uiState.currentDistance} meters")
                Text(text = "Elapsed Time: ${uiState.timerValue} seconds")
                Text(text = "Current Pace ${uiState.currentPace} m/s")

                if (!isRunStarted) {
                    Button(
                        onClick = {
                            // Only trigger the LOGIC here
                            trackingViewModel.startRun()
                        }
                    ) { Text("Start Run Session") }
                } else {
                    Button(onClick = { trackingViewModel.stopRun() }) {
                        Text("Stop Run Session")
                    }
                }
            }
        }
    }
}
