package com.example.fitnessapp.ui.activity.Stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.StatsData
import com.example.fitnessapp.ui.UiStates.StatsUIState
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import com.example.fitnessapp.ui.components.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navController: NavController,
    onHealthTrendsClick: () -> Unit = {
        navController.navigate("healthTrendsScreen"){
            popUpTo("statsScreen")
        }
    },
    onPersonalBestsClick: () -> Unit = {
        navController.navigate("PersonalBestScreen"){
            popUpTo("statsScreen")
        }
    },
) {
    val viewModel : StatsViewModel = hiltViewModel()
    val uiState by viewModel.statsUIState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf(RunFilter.DAY) }

    Scaffold(
        bottomBar = {
            BottomBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
            )

            FilterPillsRow(
                selected = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                    viewModel.onFilterSelected(filter)
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            when (val state = uiState) {
                is StatsUIState.Loading -> LoadingContent()
                is StatsUIState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.onFilterSelected(selectedFilter) }
                )
                is StatsUIState.Success -> {
                    val data = state.data
                    BigSummaryCard(
                        distance = data.totalDistance,
                        subtitle = when (selectedFilter) {
                            RunFilter.DAY -> "Total distance today"
                            RunFilter.WEEK -> "Total distance this week"
                            RunFilter.MONTH -> "Total distance this month"
                            RunFilter.ALL -> "Total distance all time"
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Performance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PastelCard(
                                icon = Icons.Filled.EmojiEvents,
                                value = data.totalRuns,
                                label = "Total Runs",
                                bgColor = Color(0xFFE0F0E8),
                                contentColor = Color(0xFF3E6B58),
                                modifier = Modifier.weight(1f)
                            )
                            PastelCard(
                                icon = Icons.Filled.LocalFireDepartment,
                                value = data.totalCalories,
                                label = "Calories Burned",
                                bgColor = Color(0xFFF8E7D9),
                                contentColor = Color(0xFF8B5E3C),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PastelCard(
                                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                                value = data.totalSteps,
                                label = "Steps",
                                bgColor = Color(0xFFF5E1F0),
                                contentColor = Color(0xFF7B3F6B),
                                modifier = Modifier.weight(1f)
                            )
                            PastelCard(
                                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                                value = data.totalDuration,
                                label = "Duration",
                                bgColor = Color(0xFFB3EBF2),
                                contentColor = Color(0xFF2E6C7B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    InsightCard(
                        title = "Health Trends",
                        subtitle = "Track your progress and patterns",
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        onClick = onHealthTrendsClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InsightCard(
                        title = "Personal Bests",
                        subtitle = "Celebrate your record achievements",
                        icon = Icons.Filled.Star,
                        onClick = onPersonalBestsClick
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun BigSummaryCard(distance: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3A5F8A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = distance,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun PastelCard(
    icon: ImageVector,
    value: String,
    label: String,
    bgColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(19.dp),
                tint = contentColor.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = contentColor.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun FilterPillsRow(
    selected: RunFilter,
    onFilterSelected: (RunFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        RunFilter.DAY to "Today",
        RunFilter.WEEK to "This week",
        RunFilter.MONTH to "This month",
        RunFilter.ALL to "All runs"
    )

    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = selected == filter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Color(0xFF3A5F8A) else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = Color(0xFF3A5F8A), strokeWidth = 4.dp, modifier = Modifier.size(46.dp))
            Text("Loading your stats...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A5F8A))
        ) {
            Text("Try Again", color = Color.White)
        }
    }
}
