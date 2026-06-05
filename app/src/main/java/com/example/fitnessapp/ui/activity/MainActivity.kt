package com.example.fitnessapp.ui.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnessapp.ui.activity.Stats.StatsScreen

import com.example.fitnessapp.ui.activity.Tracking.OsmMapview
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.ui.activity.Tracking.TrackingViewModel
import com.example.fitnessapp.ui.activity.UserProfile.ProfileScreen
import com.example.runtracker.ui.screens.RunDetailsScreen
import com.example.runtracker.ui.screens.RunHistoryScreenM3
import com.example.runtracker.ui.screens.TrackingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val trackingViewModel : TrackingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                appRun()
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun appRun() {
    val navController : NavHostController = rememberNavController()
    NavHost(navController, startDestination = "runHistory") {
        composable("runHistory", content = {RunHistoryScreenM3(navController)})
        composable("trackingScreen", content = { TrackingScreen(navController) })
        composable("statsScreen", content = { StatsScreen(navController) })
        composable("profileScreen", content = { ProfileScreen(navController) })
        composable(
            route = "runDetails/{runId}",
            arguments = listOf(navArgument("runId") { type = NavType.LongType })
        ) { backStackEntry ->
            val runId = backStackEntry.arguments?.getLong("runId") ?: -1L
            RunDetailsScreen(navController)
        }
    }
}

