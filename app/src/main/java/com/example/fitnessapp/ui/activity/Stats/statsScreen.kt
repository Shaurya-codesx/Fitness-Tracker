package com.example.fitnessapp.ui.activity.Stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
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
import com.example.fitnessapp.ui.UiStates.StatsUIState
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import com.example.fitnessapp.ui.components.BottomBar
import com.example.fitnessapp.ui.theme.*

// Shared palette — same as Auth/Home screens


private val StatsGradient = Brush.linearGradient(colors = listOf(StatsViolet, StatsVioletLight))
private val HeroGradient = Brush.linearGradient(colors = listOf(Color(0xFF7C6CF0), StatsCoral))

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
    var selectedFilter by remember { mutableStateOf(RunFilter.WEEK) }

    Scaffold(
        containerColor = StatsBg,
        bottomBar = {
            BottomBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StatsBg)
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                color = StatsTextPrimary,
                modifier = Modifier.padding(top = 16.dp, bottom = 14.dp)
            )

            FilterPillsRow(
                selected = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                    viewModel.onFilterSelected(filter)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

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

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Performance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatsTextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    // ─── BENTO GRID: asymmetric layout ───
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left: tall hero stat card
                        BentoStatCard(
                            icon = Icons.Filled.EmojiEvents,
                            value = data.totalRuns,
                            label = "Total Runs",
                            gradient = Brush.verticalGradient(listOf(StatsViolet, StatsVioletLight)),
                            contentColor = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            big = true
                        )

                        // Right: two stacked cards
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BentoStatCard(
                                icon = Icons.Filled.LocalFireDepartment,
                                value = data.totalCalories,
                                label = "Calories",
                                bgColor = Color(0xFFFFE9D6),
                                contentColor = Color(0xFF9A5B23),
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                            BentoStatCard(
                                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                                value = data.totalSteps,
                                label = "Steps",
                                bgColor = StatsMint,
                                contentColor = Color(0xFF1F6D4A),
                                modifier = Modifier.weight(1f).fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Full-width duration bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = StatsSkyBlue.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(StatsSkyBlue)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF1A5C73)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = data.totalDuration,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp),
                                color = Color(0xFF1A5C73)
                            )
                            Text(
                                text = "Total Duration",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF1A5C73).copy(alpha = 0.75f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatsTextPrimary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )

                    InsightCard(
                        title = "Health Trends",
                        subtitle = "Track your progress and patterns",
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        iconBg = StatsVioletLight.copy(alpha = 0.25f),
                        iconTint = StatsViolet,
                        onClick = onHealthTrendsClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InsightCard(
                        title = "Personal Bests",
                        subtitle = "Celebrate your record achievements",
                        icon = Icons.Filled.Star,
                        iconBg = StatsSunshine.copy(alpha = 0.35f),
                        iconTint = Color(0xFF9A7300),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = StatsCoral.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(28.dp))
            .background(HeroGradient)
            .padding(horizontal = 22.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = distance,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp
                ),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.88f)
            )
        }
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun BentoStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    bgColor: Color? = null,
    gradient: Brush? = null,
    contentColor: Color,
    big: Boolean = false
) {
    Box(
        modifier = modifier
            .shadow(
                8.dp,
                RoundedCornerShape(22.dp),
                ambientColor = (bgColor ?: StatsViolet).copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (gradient != null) Modifier.background(gradient)
                else Modifier.background(bgColor ?: StatsCard)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = if (big) Arrangement.SpaceBetween else Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(if (big) 44.dp else 34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(if (big) 22.dp else 18.dp),
                    tint = contentColor
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (big) 30.sp else 22.sp
                    ),
                    color = contentColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = iconTint.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(20.dp))
            .background(StatsCard)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatsTextPrimary))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StatsTextSecondary)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = StatsTextSecondary.copy(alpha = 0.6f)
        )
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
                    .then(
                        if (isSelected) Modifier.background(StatsGradient)
                        else Modifier.background(StatsCard)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else StatsTextSecondary
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
            CircularProgressIndicator(color = StatsViolet, strokeWidth = 4.dp, modifier = Modifier.size(46.dp))
            Text("Loading your stats...", style = MaterialTheme.typography.bodyMedium, color = StatsTextSecondary)
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
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatsTextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = StatsTextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(22.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(StatsGradient)
                .clickable { onRetry() }
                .padding(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text("Try Again", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}