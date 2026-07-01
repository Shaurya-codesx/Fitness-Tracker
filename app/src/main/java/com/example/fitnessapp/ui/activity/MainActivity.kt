package com.example.fitnessapp.ui.activity

import AuthScreen
import OnboardingScreen
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
import com.example.fitnessapp.Domain.AuthRepository
import com.example.fitnessapp.ui.activity.HomeScreen.HomeScreen
import com.example.fitnessapp.ui.activity.Stats.Distance.DistanceAnalyticsScreen
import com.example.fitnessapp.ui.activity.Stats.Energy.EnergyAnalyticsScreen
import com.example.fitnessapp.ui.activity.Stats.Pace.PaceAnalyticsScreen
import com.example.fitnessapp.ui.activity.Stats.PersonalBestsSection
import com.example.fitnessapp.ui.activity.Stats.Steps.StepsAnalyticsScreen
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.example.fitnessapp.ui.activity.Tracking.TrackingViewModel
import com.example.fitnessapp.ui.activity.UserProfile.ProfileScreen
import com.example.fitnessapp.ui.activity.Stats.StatsScreen
import com.example.fitnessapp.ui.components.HealthTrendsScreen
import com.example.runtracker.ui.screens.RunDetailsScreen
import com.example.runtracker.ui.screens.RunHistoryScreenM3
import com.example.runtracker.ui.screens.TrackingScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val trackingViewModel : TrackingViewModel by viewModels()

    // 1. CHANGED: Injected the AuthRepository directly into your Activity
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                // 2. CHANGED: Dynamically decide the starting route
                val initialScreen = if (authRepository.currentUserUid != null) {
                    "HomeScreen" // Logged in -> Go straight to dashboard
                } else {
                    "authScreen" // Logged out -> Show login
                }

                // 3. CHANGED: Passed the dynamic route into your composable
                appRun(startDestination = initialScreen)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
// 4. CHANGED: Added startDestination as a parameter
fun appRun(startDestination: String) {
    val navController : NavHostController = rememberNavController()

    // 5. CHANGED: Replaced the hardcoded "authScreen" with our dynamic variable
    NavHost(navController, startDestination = startDestination) {
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

        // Inside your NavHost in MainActivity.kt...

        composable("authScreen") {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate("HomeScreen") {
                        // Nuke the backstack so they can't hit back to login
                        popUpTo("authScreen") { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate("onboardingScreen") {
                        // Nuke the backstack so they can't hit back to login
                        popUpTo("authScreen") { inclusive = true }
                    }
                }
            )
        }

        composable("onboardingScreen") {
            OnboardingScreen(
                onSetupComplete = {
                    navController.navigate("HomeScreen") {
                        // Clear the backstack so they can't hit back to return to Onboarding
                        popUpTo("onboardingScreen") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "runDetails/{runId}",
            arguments = listOf(navArgument("runId") { type = NavType.LongType })
        ) { backStackEntry ->
            val runId = backStackEntry.arguments?.getLong("runId") ?: -1L
            RunDetailsScreen(navController)
        }
    }
}