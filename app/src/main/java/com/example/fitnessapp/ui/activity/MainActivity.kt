package com.example.fitnessapp.ui.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fitnessapp.ui.activity.HomeScreen.HomeScreen
import com.example.fitnessapp.ui.activity.Stats.Distance.DistanceAnalyticsScreen
import com.example.fitnessapp.ui.activity.Stats.Energy.EnergyAnalyticsScreen
import com.example.fitnessapp.ui.activity.Stats.Pace.PaceAnalyticsScreen
import com.example.fitnessapp.ui.activity.Stats.PersonalBestsSection
import com.example.fitnessapp.ui.activity.Stats.Steps.StepsAnalyticsScreen
import com.example.fitnessapp.ui.activity.Tracking.OsmMapview
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.ui.activity.Tracking.TrackingViewModel
import com.example.fitnessapp.ui.activity.UserProfile.ProfileScreen
import com.example.fitnessapp.ui.activity.Stats.StatsScreen
import com.example.fitnessapp.ui.components.HealthTrendsScreen
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
        composable("healthTrendsScreen", content = { HealthTrendsScreen(navController) })
        composable("stepsScreen", content = { StepsAnalyticsScreen(navController) })
        composable("distanceScreen", content = { DistanceAnalyticsScreen(navController) })
        composable("PaceScreen", content = { PaceAnalyticsScreen(navController) })
        composable("EnergyScreen", content = { EnergyAnalyticsScreen(navController) })
        composable("PersonalBestScreen", content = { PersonalBestsSection(navController) })
        composable("HomeScreen", content = { HomeScreen(navController) { }})
        composable(
            route = "runDetails/{runId}",
            arguments = listOf(navArgument("runId") { type = NavType.LongType })
        ) { backStackEntry ->
            val runId = backStackEntry.arguments?.getLong("runId") ?: -1L
            RunDetailsScreen(navController)
        }
    }
}
