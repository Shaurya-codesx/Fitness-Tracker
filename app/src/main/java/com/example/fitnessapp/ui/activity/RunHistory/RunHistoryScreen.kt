package com.example.runtracker.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.RunUiModel
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import com.example.fitnessapp.ui.activity.RunHistory.RunHistoryViewModel
import com.example.fitnessapp.ui.components.BottomBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.fitnessapp.ui.theme.*

private val HistoryGradient = Brush.linearGradient(colors = listOf(HistoryViolet, HistoryVioletLight))
private val HeroGradient = Brush.linearGradient(colors = listOf(Color(0xFF6C63B5), Color(0xFF8A7FCB)))

// ─── Filter chips data ────────────────────────────────────────────────────────

private val filterOptions = listOf("Today", "This week", "This month", "All runs")

@RequiresApi(Build.VERSION_CODES.O)
val currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

// ─── Screen Entry Point ───────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RunHistoryScreenM3(navController: NavController) {
    val runHistoryViewModel: RunHistoryViewModel = hiltViewModel()
    var selectedFilter by rememberSaveable { mutableStateOf("Today") }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    val uiState by runHistoryViewModel.runHistoryUiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = HistoryBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate("trackingScreen") {
                        popUpTo("runHistory")
                    }
                },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White) },
                text = { Text("Start new run", color = Color.White, fontWeight = FontWeight.Bold) },
                containerColor = Color.Transparent,
                shape = CircleShape,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HistoryGradient),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HistoryBg)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // Top bar
            item {
                RunHistoryTopBar(
                    totalRuns = uiState.runs.size,
                    onToggleFilters = { showFilters = !showFilters }
                )
            }

            // Filter chips
            item {
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    FilterChipRow(
                        options = filterOptions,
                        selected = selectedFilter,
                        onSelect = {
                            selectedFilter = it
                            when (selectedFilter) {
                                "All runs" -> { runHistoryViewModel.onFilterSelected(RunFilter.ALL) }
                                "Today" -> { runHistoryViewModel.onFilterSelected(RunFilter.DAY) }
                                "This week" -> { runHistoryViewModel.onFilterSelected(RunFilter.WEEK) }
                                "This month" -> { runHistoryViewModel.onFilterSelected(RunFilter.MONTH) }
                            }
                        }
                    )
                }
            }

            // Summary cards
            item {
                SummarySection(
                    totalDistance = uiState.totalDistance,
                    totalTime = uiState.totalTime,
                    totalAvgPace = uiState.totalAvgPace
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Run cards
            items(uiState.runs, key = { it.id }) { run ->
                RunSessionCardM3(run = run) { id ->
                    navController.navigate("runDetails/$id")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Empty state
            if (uiState.runs.isEmpty()) {
                item { M3EmptyState() }
            }

            // Extra space for FAB
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RunHistoryTopBar(totalRuns: Int, onToggleFilters: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Run History",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = HistoryTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$currentMonthYear · $totalRuns sessions",
                style = MaterialTheme.typography.bodySmall,
                color = HistoryTextSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(6.dp, CircleShape, ambientColor = HistoryViolet.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(HistoryGradient)
                .clickable { onToggleFilters() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.FilterList, contentDescription = "Filter", tint = Color.White)
        }
    }
}

// ─── Filter Chip Row ──────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 12.dp)
    ) {
        items(options) { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (isSelected) Modifier.background(HistoryGradient)
                        else Modifier.background(HistoryCard)
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else HistoryTextSecondary
                )
            }
        }
    }
}

// ─── Summary Section ──────────────────────────────────────────────────────────

@Composable
private fun SummarySection(totalDistance: String, totalTime: String, totalAvgPace: String) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero card — gradient
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF6C63B5).copy(alpha = 0.25f))
                .clip(RoundedCornerShape(28.dp))
                .background(HeroGradient)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = totalDistance,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
                    color = Color.White
                )
                Text(
                    text = "Total distance this month",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🏃", fontSize = 24.sp)
            }
        }

        // Bento-asymmetric row — one wide, one narrow
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TonalSummaryCard(
                value = totalAvgPace,
                label = "Min / km",
                bgColor = Color(0xFFE4E1F5),
                contentColor = Color(0xFF4A4568),
                modifier = Modifier.weight(1.2f)
            )
            TonalSummaryCard(
                value = totalTime,
                label = "Total time",
                bgColor = Color(0xFFF0E6DC),
                contentColor = Color(0xFF6B5A45),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TonalSummaryCard(
    value: String,
    label: String,
    bgColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = bgColor.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
            color = contentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor.copy(alpha = 0.8f)
        )
    }
}

// ─── Run Session Card (M3) ────────────────────────────────────────────────────

@Composable
fun RunSessionCardM3(run: RunUiModel, onClick: (Long) -> Unit) {
    val style = resolveM3CardStyle(run)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = style.badgeContainer.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(24.dp))
            .background(HistoryCard)
            .clickable { onClick(run.id) }
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = resolveRunTitle(run),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = HistoryTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = run.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = HistoryTextSecondary
                )
            }

            // Badge — pastel chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(style.badgeContainer)
            ) {
                Text(
                    text = style.badgeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = style.badgeContent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 18.dp),
            color = HistoryTextSecondary.copy(alpha = 0.15f)
        )

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            M3StatItem(icon = "⏱", value = run.duration, label = "Duration", modifier = Modifier.weight(1f))
            VerticalStatDivider()
            M3StatItem(icon = "📍", value = run.distanceInMeters + " km", label = "Distance", modifier = Modifier.weight(1f))
            VerticalStatDivider()
            M3StatItem(icon = "⚡", value = run.avgPace, label = "Avg pace", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun M3StatItem(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = HistoryTextPrimary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HistoryTextSecondary
        )
    }
}

@Composable
private fun VerticalStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(HistoryTextSecondary.copy(alpha = 0.15f))
    )
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun M3EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(HistoryVioletLight.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🏃", fontSize = 40.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No runs yet",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = HistoryTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Ready for a new run?",
            style = MaterialTheme.typography.bodyMedium,
            color = HistoryTextSecondary
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private data class M3CardStyle(
    val badgeLabel: String,
    val badgeContainer: Color,
    val badgeContent: Color
)

@Composable
private fun resolveM3CardStyle(run: RunUiModel): M3CardStyle {
    val distanceValue = run.distanceInMeters.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
    return when {
        distanceValue >= 15000 -> M3CardStyle(
            badgeLabel = "Long run",
            badgeContainer = HistorySunshine,
            badgeContent = Color(0xFF8A5A00)
        )
        distanceValue >= 8000 -> M3CardStyle(
            badgeLabel = "Personal best",
            badgeContainer = HistoryVioletLight.copy(alpha = 0.35f),
            badgeContent = HistoryViolet
        )
        else -> M3CardStyle(
            badgeLabel = "Completed",
            badgeContainer = HistoryMint,
            badgeContent = Color(0xFF1F6D4A)
        )
    }
}

private fun resolveRunTitle(run: RunUiModel): String {
    val timeString = run.startTime.uppercase()
    val hour = timeString.substringBefore(":").toIntOrNull() ?: 12
    val isPm = timeString.contains("PM")
    val isAm = timeString.contains("AM")

    val militaryHour = when {
        isPm && hour != 12 -> hour + 12
        isAm && hour == 12 -> 0
        else -> hour
    }

    return when {
        militaryHour in 5..11 -> "Morning run"
        militaryHour in 12..16 -> "Afternoon run"
        militaryHour in 17..20 -> "Evening jog"
        else -> "Night run"
    }
}