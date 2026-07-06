package com.example.fitnessapp.ui.activity.Stats

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.fitnessapp.ui.theme.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PersonalBestsSection(navController: NavController) {
    val viewModel: AnalyticsViewModel = hiltViewModel()
    val uiState by viewModel.personalBests.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LavenderBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(25.dp))
        // Header — fixed height, doesn't eat into the bento space
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Trophy Room",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = HeadingText
                )
            )
            Text(
                "Your all-time personal records",
                style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
            )
        }

        when (val state = uiState) {
            is PersonalBestUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)).background(CardWhite),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BlueAccent)
                }
            }
            is PersonalBestUiState.Empty -> {
                Column(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)).background(CardWhite),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(88.dp).clip(CircleShape).background(BlueTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = BlueAccent, modifier = Modifier.size(44.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Lace up your shoes!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = HeadingText))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Your records will appear here\nafter your first run.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                    )
                }
            }
            is PersonalBestUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(CoralTint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = CoralAccent, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                }
            }
            is PersonalBestUiState.Success -> {
                BentoTrophyLayout(state)
            }
        }
    }
}

@Composable
private fun BentoTrophyLayout(records: PersonalBestUiState.Success) {
    // This Column fills all remaining screen space via weight()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // 1. Hero banner — shorter now, weight 1f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                .padding(22.dp)
        ) {
            Box(
                modifier = Modifier.align(Alignment.TopEnd).size(48.dp).clip(CircleShape)
                    .background(HeroAccent.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = "Trophy", tint = HeroAccent, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text("FARTHEST RUN", style = MaterialTheme.typography.labelMedium.copy(color = HeroAccent, fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                Text(records.recordDistance, style = MaterialTheme.typography.displaySmall.copy(color = Color.White, fontWeight = FontWeight.ExtraBold))
            }
        }

        // 2. Bento row — big vertical card + two stacked cards, weight 1.5f (tallest section)
        Row(
            modifier = Modifier.fillMaxWidth().weight(1.5f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Big "Most Steps" card — spans full row height
            BigBentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                title = "Most steps",
                value = records.recordSteps,
                icon = Icons.Rounded.DirectionsRun,
                tint = PurpleTint,
                accent = PurpleAccent
            )

            // Stacked column: Time + Pace
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SmallBentoCard(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    title = "Longest time",
                    value = records.recordDuration,
                    icon = Icons.Rounded.Timer,
                    tint = BlueTint,
                    accent = BlueAccent
                )
                SmallBentoCard(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    title = "Fastest pace",
                    value = records.recordPace,
                    icon = Icons.Rounded.Speed,
                    tint = GreenTint,
                    accent = GreenAccent
                )
            }
        }

        // 3. Wide banner — Most Calories, weight 0.7f
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .clip(RoundedCornerShape(22.dp))
                .background(CoralTint)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Most calories burned", style = MaterialTheme.typography.bodySmall.copy(color = MutedText))
                Text(records.recordCalories, style = MaterialTheme.typography.headlineSmall.copy(color = HeadingText, fontWeight = FontWeight.ExtraBold))
            }
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun BigBentoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    accent: Color
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(22.dp)).background(tint).padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = HeadingText, fontWeight = FontWeight.ExtraBold))
            Spacer(Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = MutedText))
        }
    }
}

@Composable
private fun SmallBentoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    accent: Color
) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(tint).padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge.copy(color = HeadingText, fontWeight = FontWeight.ExtraBold))
            Text(title, style = MaterialTheme.typography.labelMedium.copy(color = MutedText))
        }
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
    }
}