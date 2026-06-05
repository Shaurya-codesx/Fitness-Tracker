package com.example.fitnessapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ─── Bottom Bar ───────────────────────────────────────────────────────────────

@Composable
fun BottomBar(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 0.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Text("🏠", fontSize = 20.sp) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = {
                navController.navigate("runHistory") {
                    popUpTo("statsScreen")
                }
            },
            icon = { Text("🏃", fontSize = 20.sp) },
            label = { Text("History") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("statsScreen") {
                    popUpTo("runHistory")
                }
            },
            icon = { Text("📊", fontSize = 20.sp) },
            label = { Text("Stats") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("profileScreen") {
                    popUpTo("runHistory")
                }
            },
            icon = { Text("👤", fontSize = 20.sp) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}