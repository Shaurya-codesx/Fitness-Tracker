package com.example.fitnessapp.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.fitnessapp.Data.Model.AppDatabase
import com.example.fitnessapp.Data.Model.LocationPoints
import com.example.fitnessapp.Data.Model.Entities.RunEntity
import com.example.fitnessapp.Data.Model.runDAO
import com.example.fitnessapp.Data.Repositories.RunRepoImpl
import com.example.fitnessapp.Domain.UseCases.CalcDistanceUseCase
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.getDAO()
            val dist = CalcDistanceUseCase()

            val repo = RunRepoImpl(dao, dist)
            repo.startRun()
            delay(2000)
            repo.addLocationPoint(LocationPoints(
                GeoPoint(28.440548, 77.297560),
                System.currentTimeMillis()))
            delay(2000)
            repo.addLocationPoint(LocationPoints(
                GeoPoint(28.440548, 77.297975),
                System.currentTimeMillis()))
            delay(4000)
            repo.addLocationPoint(LocationPoints(
                GeoPoint(28.440548, 77.298549),
                System.currentTimeMillis()))
            repo.stopRun()
            repo.getAllRuns().collect {
                Log.d("checkRun", it.toString())
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FitnessAppTheme {
        Greeting("Android")
    }
}