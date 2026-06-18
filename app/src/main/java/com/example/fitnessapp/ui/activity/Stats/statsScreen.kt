package com.example.fittracker.ui.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.StatsData
import com.example.fitnessapp.ui.UiStates.StatsUIState
import com.example.fitnessapp.ui.activity.RunHistory.RunFilter
import com.example.fitnessapp.ui.activity.Stats.StatsViewModel

// ─────────────────────────────────────────────────────────────────
//  Entry point
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController) {
    val viewModel : StatsViewModel = hiltViewModel()
    val uiState by viewModel.statsUIState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { StatsTopBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Filter chip row
            FilterRow(
                selected = selectedFilter,
                onSelect = viewModel::onFilterSelected
            )

            Spacer(Modifier.height(8.dp))

            // Content area — animated crossfade between states
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "stats_content"
            ) { state ->
                when (state) {
                    is StatsUIState.Loading -> StatsLoadingContent()
                    is StatsUIState.Error   -> StatsErrorContent(message = state.message)
                    is StatsUIState.Success -> StatsSuccessContent(data = state.data)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Top Bar
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

// ─────────────────────────────────────────────────────────────────
//  Filter chips
// ─────────────────────────────────────────────────────────────────

private val filters = listOf(
    RunFilter.DAY   to "Today",
    RunFilter.WEEK  to "This week",
    RunFilter.MONTH to "This month",
    RunFilter.ALL   to "All runs"
)

@Composable
private fun FilterRow(
    selected: RunFilter,
    onSelect: (RunFilter) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = filters.indexOfFirst { it.first == selected }.coerceAtLeast(0),
        edgePadding = 16.dp,
        divider = {},
        indicator = {},           // we handle selection inside each chip
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(filter) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor         = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor             = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled          = true,
                    selected         = isSelected,
                    borderColor      = Color.Transparent,
                    selectedBorderColor = Color.Transparent
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Success content
// ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsSuccessContent(data: StatsData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero card – Distance gets the spotlight
        HeroStatCard(
            label    = "Distance",
            value    = data.totalDistance,
            icon     = Icons.Rounded.DirectionsRun,
            gradient = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary
            )
        )

        // 3-metric row underneath
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallStatCard(
                modifier = Modifier.weight(1f),
                label    = "Calories",
                value    = data.totalCalories,
                icon     = Icons.Outlined.LocalFireDepartment,
                tint     = MaterialTheme.colorScheme.error
            )
            SmallStatCard(
                modifier = Modifier.weight(1f),
                label    = "Steps",
                value    = data.totalSteps,
                icon     = Icons.Outlined.DoNotStep,
                tint     = MaterialTheme.colorScheme.secondary
            )
            SmallStatCard(
                modifier = Modifier.weight(1f),
                label    = "Runs",
                value    = data.totalRuns,
                icon     = Icons.Outlined.FitnessCenter,
                tint     = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(Modifier.height(8.dp))

        // Action cards
        ActionCard(
            title    = "Health Trends",
            subtitle = "View your progress over time",
            icon     = Icons.Outlined.TrendingUp,
            onClick  = { /* navigate */ }
        )
        ActionCard(
            title    = "Personal Bests",
            subtitle = "Your all-time records",
            icon     = Icons.Outlined.EmojiEvents,
            onClick  = { /* navigate */ }
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
//  Hero stat card (Distance)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroStatCard(
    label   : String,
    value   : String,
    icon    : ImageVector,
    gradient: List<Color>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_gradient")
    val gradientShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = gradient,
                    start  = Offset(0f, gradientShift * 400f),
                    end    = Offset(400f + gradientShift * 200f, 400f)
                )
            )
            .padding(24.dp)
    ) {
        // Background decorative icon
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = Color.White.copy(alpha = 0.12f),
            modifier           = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 16.dp, y = 16.dp)
        )

        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = Color.White.copy(alpha = 0.85f),
                    modifier           = Modifier.size(18.dp)
                )
                Text(
                    text  = label.uppercase(),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp
                    )
                )
            }
            Text(
                text  = value,
                color = Color.White,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Small stat card
// ─────────────────────────────────────────────────────────────────

@Composable
private fun SmallStatCard(
    modifier : Modifier = Modifier,
    label    : String,
    value    : String,
    icon     : ImageVector,
    tint     : Color
) {
    Card(
        modifier = modifier.height(110.dp),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = tint,
                    modifier           = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text  = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Action card (Health Trends / Personal Bests)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ActionCard(
    title   : String,
    subtitle: String,
    icon    : ImageVector,
    onClick : () -> Unit
) {
    Card(
        onClick   = onClick,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier           = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text  = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                imageVector        = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Loading state — shimmer skeleton
// ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsLoadingContent() {
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by shimmerTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val shimmerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = shimmerAlpha * 0.15f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerColor)
        )

        // Small cards skeleton row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(shimmerColor)
                )
            }
        }

        // Action card skeletons
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(shimmerColor)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Error state
// ─────────────────────────────────────────────────────────────────

@Composable
private fun StatsErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onErrorContainer,
                modifier           = Modifier.size(36.dp)
            )
        }

        Text(
            text      = "Something went wrong",
            style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color     = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = { /* viewModel.retry() if you expose it */ },
            shape   = RoundedCornerShape(14.dp),
            colors  = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector        = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Try again")
        }
    }
}