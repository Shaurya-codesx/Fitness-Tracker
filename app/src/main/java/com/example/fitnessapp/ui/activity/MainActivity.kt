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
import com.example.fitnessapp.ui.theme.FitnessAppTheme
import com.google.firebase.firestore.GeoPoint
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

            val dummyRun = RunEntity (
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis() + 10000,
                distanceInMeters = 1000f,
                avgPace = 300f,
                route = listOf(LocationPoints(GeoPoint(23.44, 34.3), System.currentTimeMillis())),
                isSynced = false
            )
            dao.insertRun(dummyRun)
            dao.getAllRuns().collect {
                Log.d("DB_TEST", it.toString())
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