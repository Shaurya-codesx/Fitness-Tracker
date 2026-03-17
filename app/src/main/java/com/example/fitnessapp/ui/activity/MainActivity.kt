package com.example.fitnessapp.ui.activity

import android.content.Intent
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.Data.Location.androidLocationProvider
import com.example.fitnessapp.Data.Model.AppDatabase
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.Data.Repositories.RunRepoImpl
import com.example.fitnessapp.Data.Service.LocationForegroundService
import com.example.fitnessapp.Domain.LocationDataSource
import com.example.fitnessapp.Domain.UseCases.CalcDistanceUseCase
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.ui.viewModel.TrackingViewModel
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val trackingViewModel : TrackingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                Uitesting(trackingViewModel)
            }
        }
    }
}


@Composable
@Preview(showSystemUi = true, showBackground = true)
fun Uitesting(trackingViewModel: TrackingViewModel) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .shadow(15.dp, RoundedCornerShape(20.dp), spotColor = Color.Red)
            .fillMaxSize()
            .clip(RoundedCornerShape(20)),
        contentAlignment = Alignment.Center
    ){
        Column() {
            Button(
                onClick ={
                    trackingViewModel.startRun()
                }
            ) {Text("Start Run Session") }
            Button(onClick = {trackingViewModel.stopRun()}) { Text("Stop Run Session")}
        }
    }
}
