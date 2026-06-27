package com.example.fitnessapp.ui.activity.HomeScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.ModeEdit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.HomeUiState
import com.example.fitnessapp.ui.utils.UserGoals
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.ui.geometry.Size

// ─── THEME COLORS ────────────────────────────────────────────────────────
private val BgColor = Color(0xFFF2F1F8)
private val CardBg = Color.White
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF8888A8)

// Ring Colors
private val ColorSteps = Color(0xFFA78BFA)    // Purple
private val ColorDistance = Color(0xFF60A5FA)  // Blue
private val ColorCalories = Color(0xFFF87171)  // Red

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onStartRunClick: () -> Unit // Pass your navigation event here!
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.homeUiState.collectAsState(initial = HomeUiState.Loading)

    // Bottom Sheet State
    var showGoalSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BgColor,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartRunClick,
                containerColor = Color(0xFF1A1A2E),
                contentColor = Color.White,
                shape = RoundedCornerShape(100),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Rounded.DirectionsRun, contentDescription = "Start Run")
                Spacer(Modifier.width(8.dp))
                Text("Start Run", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Success -> {
                    DashboardContent(
                        state = state,
                        onEditGoalsClick = { showGoalSheet = true }
                    )

                    // Goal Setting Bottom Sheet
                    if (showGoalSheet) {
                        GoalSettingBottomSheet(
                            sheetState = sheetState,
                            initialGoals = state.userGoals,
                            onDismiss = { showGoalSheet = false },
                            onSave = { steps, dist, cal ->
                                viewModel.saveNewGoals(steps, dist, cal)
                                showGoalSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DashboardContent(
    state: HomeUiState.Success,
    onEditGoalsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. HEADER (Greeting & Streak)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Good morning,", style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary))
                Text(state.userName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
            }

            // Streak Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFEF3C7)) // Light Amber
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("${state.currentStreak} Day Streak", style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF92400E), fontWeight = FontWeight.Bold))
            }
        }

        // 2. GOAL PROGRESS RINGS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardBg)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Today's Targets", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    IconButton(onClick = onEditGoalsClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Goals", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!state.userGoals.areGoalsSet) {
                    // Empty State if goals aren't set
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).clickable { onEditGoalsClick() }, contentAlignment = Alignment.Center) {
                        Text("Tap here to set your daily goals!", color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                } else {
                    // Concentric Rings
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ConcentricGoalRings(
                            modifier = Modifier.size(140.dp),
                            stepsPct = (state.todaySteps.toFloat() / state.userGoals.stepsGoal!!).coerceIn(0f, 1f),
                            distPct = (state.todayDistanceKm / state.userGoals.distanceGoalKm!!).coerceIn(0f, 1f),
                            calPct = (state.todayCalories.toFloat() / state.userGoals.caloriesGoal!!).coerceIn(0f, 1f)
                        )

                        Spacer(modifier = Modifier.width(24.dp))

                        // Legend
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LegendItem(ColorSteps, "Steps", "${state.todaySteps} / ${state.userGoals.stepsGoal}")
                            LegendItem(ColorDistance, "Distance", "${String.format("%.1f", state.todayDistanceKm)} / ${state.userGoals.distanceGoalKm} km")
                            LegendItem(ColorCalories, "Calories", "${state.todayCalories} / ${state.userGoals.caloriesGoal} kcal")
                        }
                    }
                }
            }
        }

        // 3. CURRENT MONTH HEATMAP
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardBg)
                .padding(24.dp)
        ) {
            Column {
                Text("Consistency", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                Text(LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))

                Spacer(modifier = Modifier.height(16.dp))
                MonthlyHeatmap(activeDates = state.activeDaysThisMonth)
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
    }
}

// ─── CUSTOM CANVAS RINGS ──────────────────────────────────────────────────


@Composable
private fun ConcentricGoalRings(modifier: Modifier = Modifier, stepsPct: Float, distPct: Float, calPct: Float) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val spacing = 4.dp.toPx()

        // Calculate the radius for each ring
        val center = size.width / 2f
        val outerRadius = center - (strokeWidth / 2f)
        val midRadius = outerRadius - strokeWidth - spacing
        val innerRadius = midRadius - strokeWidth - spacing

        // Background tracks (These were working fine!)
        drawCircle(color = ColorSteps.copy(alpha = 0.2f), radius = outerRadius, style = Stroke(strokeWidth))
        drawCircle(color = ColorDistance.copy(alpha = 0.2f), radius = midRadius, style = Stroke(strokeWidth))
        drawCircle(color = ColorCalories.copy(alpha = 0.2f), radius = innerRadius, style = Stroke(strokeWidth))

        // Helper functions to calculate the exact bounding box for each arc
        fun arcTopLeft(radius: Float) = Offset(size.width / 2f - radius, size.height / 2f - radius)
        fun arcSize(radius: Float) = Size(radius * 2f, radius * 2f)

        // Progress arcs (Now constrained to their specific bounding boxes!)
        drawArc(
            color = ColorSteps,
            startAngle = -90f,
            sweepAngle = stepsPct * 360f,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
            topLeft = arcTopLeft(outerRadius),
            size = arcSize(outerRadius)
        )

        drawArc(
            color = ColorDistance,
            startAngle = -90f,
            sweepAngle = distPct * 360f,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
            topLeft = arcTopLeft(midRadius),
            size = arcSize(midRadius)
        )

        drawArc(
            color = ColorCalories,
            startAngle = -90f,
            sweepAngle = calPct * 360f,
            useCenter = false,
            style = Stroke(strokeWidth, cap = StrokeCap.Round),
            topLeft = arcTopLeft(innerRadius),
            size = arcSize(innerRadius)
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Text(value, style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        }
    }
}

// ─── MONTHLY CALENDAR HEATMAP ─────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthlyHeatmap(activeDates: Set<LocalDate>) {
    val today = LocalDate.now()
    val yearMonth = YearMonth.now()
    val daysInMonth = yearMonth.lengthOfMonth()

    // 1 (Monday) to 7 (Sunday). Subtract 1 so Monday starts at index 0.
    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value - 1

    val totalCells = daysInMonth + firstDayOffset

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(200.dp),
        userScrollEnabled = false
    ) {
        items(totalCells) { index ->
            if (index < firstDayOffset) {
                Box(modifier = Modifier.aspectRatio(1f)) // Empty leading space
            } else {
                val day = index - firstDayOffset + 1
                val date = yearMonth.atDay(day)
                val isActive = activeDates.contains(date)
                val isToday = date == today

                Box(
                    modifier = Modifier.aspectRatio(1f).padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isActive -> ColorDistance // Highlight run days
                                    isToday -> Color(0xFFE5E7EB) // Light gray for today if inactive
                                    else -> Color(0xFFF3F4F6) // Very light gray for future/past empty days
                                }
                            )
                    ) {
                        // Optional: Show day numbers
                        Text(
                            text = day.toString(),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isActive) Color.White else TextSecondary,
                                fontWeight = if (isToday || isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

// ─── GOAL SETTING BOTTOM SHEET ────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingBottomSheet(
    sheetState: SheetState,
    initialGoals: UserGoals,
    onDismiss: () -> Unit,
    onSave: (Int, Float, Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var stepInput by remember { mutableStateOf(initialGoals.stepsGoal?.toString() ?: "10000") }
    var distInput by remember { mutableStateOf(initialGoals.distanceGoalKm?.toString() ?: "5.0") }
    var calInput by remember { mutableStateOf(initialGoals.caloriesGoal?.toString() ?: "500") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Set Daily Targets", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

            OutlinedTextField(
                value = stepInput,
                onValueChange = { stepInput = it },
                label = { Text("Daily Steps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = distInput,
                onValueChange = { distInput = it },
                label = { Text("Daily Distance (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = calInput,
                onValueChange = { calInput = it },
                label = { Text("Daily Calories (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val steps = stepInput.toIntOrNull() ?: 10000
                    val dist = distInput.toFloatOrNull() ?: 5.0f
                    val cals = calInput.toIntOrNull() ?: 500
                    scope.launch {
                        sheetState.hide()
                        onSave(steps, dist, cals)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Targets", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}