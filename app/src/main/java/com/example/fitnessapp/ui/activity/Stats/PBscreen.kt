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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.PersonalBestUiState
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.R


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PersonalBestsScreen(navController: NavController) {
    val viewModel: AnalyticsViewModel = hiltViewModel()
    val uiState by viewModel.personalBests.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundPB)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ==================== HEADER ====================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .shadow(elevation = 2.dp, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.pb_back_desc),
                    tint = HeadingTextPB,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.pb_trophy_room_title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = HeadingTextPB
                    )
                )
                Text(
                    text = stringResource(R.string.pb_trophy_room_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MutedTextPB, fontWeight = FontWeight.Medium)
                )
            }
        }

        // ==================== CONTENT STATE ====================
        when (val state = uiState) {
            is PersonalBestUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(32.dp))
                        .background(CardWhitePB),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = HeroGradientStartPB)
                }
            }
            is PersonalBestUiState.Empty -> {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhitePB),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(HeroGradientEndPB.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = HeroGradientEndPB, modifier = Modifier.size(48.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.pb_empty_title),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, color = HeadingTextPB)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.pb_empty_subtitle),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge.copy(color = MutedTextPB)
                        )
                    }
                }
            }
            is PersonalBestUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = PaceBgPB),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = PaceAccentPB, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
            is PersonalBestUiState.Success -> {
                BentoTrophyLayout(state)
            }
        }
    }
}

// ==================== BENTO LAYOUT ARCHITECTURE ====================
@Composable
private fun BentoTrophyLayout(records: PersonalBestUiState.Success) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // 1. Hero banner — Gradient Distance (Weight 1f)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(HeroGradientStartPB, HeroGradientEndPB)))
                    .padding(24.dp)
            ) {
                // Decorative Trophy Top Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = stringResource(R.string.pb_trophy_desc),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Data Bottom Left
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Text(
                        text = stringResource(R.string.pb_farthest_run),
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = records.recordDistance,
                        style = MaterialTheme.typography.displayMedium.copy(color = Color.White, fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }

        // 2. Bento row — Vertical card + Stacked cards (Weight 1.5f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Big "Most Steps" card
            BigBentoCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = stringResource(R.string.pb_most_steps),
                value = records.recordSteps,
                icon = Icons.Rounded.DirectionsRun,
                backgroundColor = StepsBgPB,
                accentColor = StepsAccentPB
            )

            // Stacked column: Time + Pace
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallBentoCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = stringResource(R.string.pb_longest_time),
                    value = records.recordDuration,
                    icon = Icons.Rounded.Timer,
                    backgroundColor = TimeBgPB,
                    accentColor = TimeAccentPB
                )
                SmallBentoCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    title = stringResource(R.string.pb_fastest_pace),
                    value = records.recordPace,
                    icon = Icons.Rounded.Speed,
                    backgroundColor = PaceBgPB,
                    accentColor = PaceAccentPB
                )
            }
        }

        // 3. Wide banner — Calories (Weight 0.7f)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CaloriesBgPB),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.pb_most_calories),
                        style = MaterialTheme.typography.bodyMedium.copy(color = CaloriesAccentPB.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = records.recordCalories,
                        style = MaterialTheme.typography.headlineMedium.copy(color = CaloriesAccentPB, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CardWhitePB.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = CaloriesAccentPB, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

// ==================== BENTO CARD COMPONENTS ====================

@Composable
private fun BigBentoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CardWhitePB.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp))
            }
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium.copy(color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp))
                Spacer(Modifier.height(4.dp))
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(color = accentColor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun SmallBentoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(CardWhitePB.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge.copy(color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp))
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = accentColor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold))
            }
        }
    }
}