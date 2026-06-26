package com.example.fitnessapp.ui.activity.Stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.PersonalBestUiState

// ─── PREMIUM TROPHY COLORS ───────────────────────────────────────────────────
private val GoldStart = Color(0xFFFFD700)
private val GoldEnd = Color(0xFFF59E0B)
private val DarkTrophyBg = Color(0xFF1A1A2E)
private val SubTrophyBg = Color(0xFF252542)
private val LightText = Color.White
private val MutedText = Color(0xFF8888A8)

@Composable
fun PersonalBestsSection(navController: NavController) {
    val viewModel : AnalyticsViewModel = hiltViewModel()
    val uiState by viewModel.personalBests.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Text(
            text = "Trophy Room",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = DarkTrophyBg
            )
        )

        // State Machine UI
        val state = uiState
        when (state) {
            is PersonalBestUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldEnd)
                }
            }
            is PersonalBestUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lace up your shoes!\nYour records will appear here after your first run.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                    )
                }
            }
            is PersonalBestUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFFFEE2E2)).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = Color(0xFFB91C1C))
                }
            }
            is PersonalBestUiState.Success -> {
                TrophyGrid(state)
            }
        }
    }
}

@Composable
private fun TrophyGrid(records: PersonalBestUiState.Success) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // 1. Hero Trophy (Farthest Distance)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(DarkTrophyBg, Color(0xFF111122))))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Farthest Run", style = MaterialTheme.typography.labelMedium.copy(color = GoldStart, fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(records.recordDistance, style = MaterialTheme.typography.headlineMedium.copy(color = LightText, fontWeight = FontWeight.Bold))
                }
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GoldStart, GoldEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.EmojiEvents, contentDescription = "Trophy", tint = DarkTrophyBg, modifier = Modifier.size(32.dp))
                }
            }
        }

        // 2. The 2x2 Sub-Trophy Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SubTrophyCard(
                modifier = Modifier.weight(1f),
                title = "Longest Time",
                value = records.recordDuration,
                icon = Icons.Rounded.Timer,
                iconTint = Color(0xFF60A5FA) // Blue
            )
            SubTrophyCard(
                modifier = Modifier.weight(1f),
                title = "Fastest Pace",
                value = records.recordPace,
                icon = Icons.Rounded.Speed,
                iconTint = Color(0xFF34D399) // Green
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SubTrophyCard(
                modifier = Modifier.weight(1f),
                title = "Most Calories",
                value = records.recordCalories,
                icon = Icons.Rounded.LocalFireDepartment,
                iconTint = Color(0xFFF87171) // Red
            )
            SubTrophyCard(
                modifier = Modifier.weight(1f),
                title = "Most Steps",
                value = records.recordSteps,
                icon = Icons.Rounded.DirectionsRun,
                iconTint = Color(0xFFA78BFA) // Purple
            )
        }
    }
}

@Composable
private fun SubTrophyCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SubTrophyBg)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Text(title, style = MaterialTheme.typography.labelSmall.copy(color = MutedText, fontWeight = FontWeight.Medium))
            }
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = LightText, fontWeight = FontWeight.Bold))
        }
    }
}