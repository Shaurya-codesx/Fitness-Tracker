package com.example.runtracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnessapp.ui.UiStates.RunUiModel
import com.example.fitnessapp.ui.activity.RunHistory.RunHistoryViewModel
import com.example.fitnessapp.ui.activity.Tracking.TrackingViewModel
import com.example.fitnessapp.ui.activity.Uitesting


// ─── Filter chips data ────────────────────────────────────────────────────────

private val filterOptions = listOf("All runs", "This week", "This month")

// ─── Screen Entry Point ───────────────────────────────────────────────────────

@Composable
fun RunHistoryScreenM3() {
    val runHistoryViewModel: RunHistoryViewModel = hiltViewModel()
    val uiState by runHistoryViewModel.runHistoryUiState.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("All runs") }


    // M3 scaffold with bottom bar
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* navigate to start run */
                },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Start new run") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = { RunHistoryBottomBar() }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // Top bar
            item {
                RunHistoryTopBar(totalRuns = uiState.runs.size)
            }

            // Filter chips
            item {
                FilterChipRow(
                    options = filterOptions,
                    selected = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }

            // Summary cards
            item {
                SummarySection(runs = uiState.runs)
            }

            // Section header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent sessions",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = {}) {
                        Text("See all", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Run cards
            items(uiState.runs, key = { it.id }) { run ->
                RunSessionCardM3(run = run)
                Spacer(modifier = Modifier.height(10.dp))
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

@Composable
private fun RunHistoryTopBar(totalRuns: Int) {
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
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "March 2026 · $totalRuns sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tonal icon button
        FilledTonalIconButton(
            onClick = {},
            shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Icon(Icons.Rounded.FilterList, contentDescription = "Filter")
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
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option) },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

// ─── Summary Section ──────────────────────────────────────────────────────────

@Composable
private fun SummarySection(runs: List<RunUiModel>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero card — primary color
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "42.6 km",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Total distance this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏃", fontSize = 22.sp)
                }
            }
        }

        // Two tonal cards in a row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TonalSummaryCard(
                value = "6:12",
                label = "Avg pace / km",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            TonalSummaryCard(
                value = "5h 24m",
                label = "Total time",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TonalSummaryCard(
    value: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f)
            )
        }
    }
}

// ─── Run Session Card (M3) ────────────────────────────────────────────────────

@Composable
fun RunSessionCardM3(run: RunUiModel) {
    val style = resolveM3CardStyle(run)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${run.startTime} – ${run.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge — tonal chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = style.badgeContainer,
                ) {
                    Text(
                        text = style.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = style.badgeContent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                M3StatItem(icon = "⏱", value = run.duration, label = "Duration", modifier = Modifier.weight(1f))
                VerticalStatDivider()
                M3StatItem(icon = "📍", value = run.distanceInMeters, label = "Distance", modifier = Modifier.weight(1f))
                VerticalStatDivider()
                M3StatItem(icon = "⚡", value = run.avgPace, label = "Avg pace", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun M3StatItem(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VerticalStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────

@Composable
private fun RunHistoryBottomBar() {
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
            onClick = {},
            icon = { Text("🏃", fontSize = 20.sp) },
            label = { Text("History") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Text("📊", fontSize = 20.sp) },
            label = { Text("Stats") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Text("👤", fontSize = 20.sp) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun M3EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏃", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "No runs yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Start your first session!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            badgeContainer = MaterialTheme.colorScheme.tertiaryContainer,
            badgeContent = MaterialTheme.colorScheme.onTertiaryContainer
        )
        distanceValue >= 8000 -> M3CardStyle(
            badgeLabel = "Personal best",
            badgeContainer = MaterialTheme.colorScheme.secondaryContainer,
            badgeContent = MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> M3CardStyle(
            badgeLabel = "Completed",
            badgeContainer = Color(0xFFC8E6C9),
            badgeContent = Color(0xFF1B5E20)
        )
    }
}

private fun resolveRunTitle(run: RunUiModel): String {
    val hour = run.startTime.substringBefore(":").toIntOrNull() ?: 12
    return when {
        hour < 10 -> "Morning run"
        hour < 14 -> "Midday run"
        hour < 18 -> "Afternoon run"
        else -> "Evening jog"
    }
}