package com.example.fitnessapp.ui.activity.Stats.Energy

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fitnessapp.Data.Model.StatsDataClasses.EnergySplitData
import com.example.fitnessapp.ui.activity.Stats.AnalyticsViewModel
import com.example.fitnessapp.ui.activity.Stats.FilterRange

import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.datetime.DayOfWeek

// ─────────────────────────────────────────────
// Design Tokens – Yellow/Energy Theme!
// ─────────────────────────────────────────────
private val BackgroundColor   = Color(0xFFF2F1F8)
private val HeroCardColor     = Color(0xFFF59E0B) // Vibrant Amber/Orange
private val HeroCardAccent    = Color(0xFFFBBF24) // Bright Yellow

private val ChipSelectedBg    = Color(0xFFF59E0B)
private val ChipSelectedText  = Color(0xFFFFFFFF)
private val ChipUnselectedBg  = Color(0xFFE8E6F0)
private val ChipUnselectedText= Color(0xFF5C5C7A)

private val StatCardGreen     = Color(0xFFDFF2E1)
private val StatCardOrange    = Color(0xFFFDEDD8)
private val StatCardTeal      = Color(0xFFD6F2EF)

private val OnHeroText        = Color(0xFFFFFFFF)
private val OnHeroSubText     = Color(0xFFFFFBEB) // Very light yellow for readability

private val CardRadius        = 20.dp
private val SectionPadding    = 20.dp


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnergyAnalyticsScreen(navController: NavController) {
    val viewModel: AnalyticsViewModel = hiltViewModel()
    var selectedFilter by remember { mutableStateOf(FilterRange.WEEK) }
    val scope = rememberCoroutineScope()

    val initialPage = 10_000
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 20_000 })

    LaunchedEffect(selectedFilter) {
        pagerState.animateScrollToPage(initialPage)
    }

    val currentOffset = pagerState.currentPage - initialPage
    val headerText = remember(selectedFilter, currentOffset) {
        getFormattedHeader(selectedFilter, currentOffset)
    }

    val currentUiState by viewModel
        .getEnergyDataForPage(selectedFilter, currentOffset)
        .collectAsState(initial = EnergyUiState())

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SectionPadding)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ── Back Button ──────────────────────────────────────────
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(40.dp).clip(CircleShape).background(ChipUnselectedBg)
            ) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = ChipUnselectedText, modifier = Modifier.size(24.dp))
            }

            // ── 1. Screen title ──────────────────────────────────────────
            Text(
                text = "Energy",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
            )

            // ── 2. Filter chips ──────────────────────────────────────────
            FilterChipRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // ── 3. Hero summary card ─────────────────────────────────────
            HeroSummaryCard(
                headerText = headerText,
                uiState = currentUiState,
                onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext    = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            )

            // ── 4. Quick stat row ────────────────────────────────────────
            if (currentUiState.chartData.isNotEmpty()) {
                QuickStatsRow(uiState = currentUiState, selectedFilter = selectedFilter)
            }

            // ── 5. Swipeable BAR chart ───────────────────────────────────
            ChartCard {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                ) { page ->
                    val pageOffset = page - initialPage
                    val uiState by viewModel
                        .getEnergyDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = EnergyUiState())

                    if (uiState.chartData.any { it.value > 0f }) {
                        EnergyBarChart(chartData = uiState.chartData)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No calories burned", style = MaterialTheme.typography.bodyMedium, color = ChipUnselectedText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = initialPage)
            }

            // ── 6. Intensity Split Donut Chart (LIGHT THEME) ──────────────
            if (currentUiState.chartData.any { it.value > 0f }) {
                EnergySplitCard(splitData = currentUiState.energySplit)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Summary Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroSummaryCard(headerText: String, uiState: EnergyUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Brush.linearGradient(colors = listOf(HeroCardColor, HeroCardAccent)))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                    Icon(Icons.Rounded.ChevronLeft, "Previous", tint = OnHeroText, modifier = Modifier.size(20.dp))
                }
                Text(headerText, style = MaterialTheme.typography.titleLarge.copy(color = OnHeroText, fontWeight = FontWeight.Medium))
                IconButton(onClick = onNext, modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                    Icon(Icons.Rounded.ChevronRight, "Next", tint = OnHeroText, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.25f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = OnHeroText, modifier = Modifier.size(14.dp))
                    Text("Avg. Daily Burn", style = MaterialTheme.typography.labelSmall.copy(color = OnHeroText))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.chartData.any { it.value > 0f }) {
                Text(
                    text = uiState.dailyAverage.toInt().toString(), // Whole numbers for calories
                    style = MaterialTheme.typography.displaySmall.copy(color = OnHeroText, fontWeight = FontWeight.Bold, fontSize = 48.sp)
                )
                Text("kcal", style = MaterialTheme.typography.titleMedium.copy(color = OnHeroSubText))
            } else {
                Box(modifier = Modifier.height(72.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.1f))) {}
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick Stats Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickStatsRow(uiState: EnergyUiState, selectedFilter: FilterRange) {
    val bestDay = uiState.chartData.maxByOrNull { it.value }
    val (peakLabel, activeLabel, activeUnit) = when (selectedFilter) {
        FilterRange.WEEK -> Triple("Highest burn", "Active days", "days")
        FilterRange.MONTH -> Triple("Best week", "Active weeks", "weeks")
        FilterRange.YEAR -> Triple("Best month", "Active months", "months")
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardGreen,
            label    = "Total Burn",
            value    = uiState.totalCalories.toInt().toString(),
            unit     = "kcal"
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardOrange,
            label    = peakLabel,
            value    = bestDay?.displayLabel ?: "–",
            unit     = "${bestDay?.value?.toInt() ?: 0} kcal"
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardTeal,
            label    = activeLabel,
            value    = uiState.chartData.count { it.value > 0f }.toString(),
            unit     = activeUnit
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Light Theme Intensity Split Donut Chart
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EnergySplitCard(splitData: EnergySplitData) {
    val hasData = splitData.lightRecoveryPct > 0f || splitData.solidEffortPct > 0f || splitData.epicBurnPct > 0f

    // Light theme colors to match the rest of the page layout
    val CardBackground = Color.White
    val DarkText = Color(0xFF1A1A2E)
    val MutedText = Color(0xFF8888A8)

    // Vibrant colors matching the yellowish/warm theme for intensity
    val ColorRecovery = Color(0xFF4ADE80) // Light Green
    val ColorSolid    = Color(0xFFFBBF24) // Yellow/Amber (Ties to Hero card)
    val ColorEpic     = Color(0xFFF87171) // Soft Red

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(20.dp)
    ) {
        Text(
            text = "Workout Intensity",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkText)
        )
        Text(
            text = "by caloric burn",
            style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Canvas(
            modifier = Modifier.size(180.dp).align(Alignment.CenterHorizontally)
        ) {
            val strokeWidth = 35.dp.toPx()
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            var startAngle = -90f

            if (!hasData) {
                drawArc(color = Color(0xFFF2F1F8), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
            } else {
                val sweep1 = (splitData.lightRecoveryPct / 100f) * 360f
                if (sweep1 > 0) {
                    drawArc(color = ColorRecovery, startAngle = startAngle, sweepAngle = sweep1, useCenter = false, style = stroke)
                    startAngle += sweep1
                }

                val sweep2 = (splitData.solidEffortPct / 100f) * 360f
                if (sweep2 > 0) {
                    drawArc(color = ColorSolid, startAngle = startAngle, sweepAngle = sweep2, useCenter = false, style = stroke)
                    startAngle += sweep2
                }

                val sweep3 = (splitData.epicBurnPct / 100f) * 360f
                if (sweep3 > 0) {
                    drawArc(color = ColorEpic, startAngle = startAngle, sweepAngle = sweep3, useCenter = false, style = stroke)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendItem(color = ColorRecovery, label = "Light (< 300 kcal)", percentage = splitData.lightRecoveryPct)
            LegendItem(color = ColorSolid, label = "Solid (300 - 600 kcal)", percentage = splitData.solidEffortPct)
            LegendItem(color = ColorEpic, label = "Epic (> 600 kcal)", percentage = splitData.epicBurnPct)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bar Chart & Reusable Helpers
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EnergyBarChart(chartData: List<ChartData>) {
    val model = CartesianChartModel(
        ColumnCartesianLayerModel.build {
            series(chartData.map { it.value })
        }
    )
    val bottomAxisValueFormatter = CartesianValueFormatter { x, _, _ ->
        chartData.getOrNull(x.toInt())?.displayLabel ?: ""
    }
    // Formats Y-axis as whole numbers (e.g., 400, 600)
    val startAxisValueFormatter = CartesianValueFormatter { y, _, _ ->
        y.toInt().toString()
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        color = HeroCardColor,
                        thickness = 14.dp,
                        shape = remember { Shape.rounded(allPercent = 100) }
                    )
                )
            ),
            startAxis = rememberStartAxis(
                label = rememberAxisLabelComponent(color = ChipUnselectedText),
                valueFormatter = startAxisValueFormatter,
                guideline = rememberAxisGuidelineComponent(color = Color.LightGray.copy(alpha = 0.5f))
            ),
            bottomAxis = rememberBottomAxis(
                label = rememberAxisLabelComponent(color = ChipUnselectedText),
                valueFormatter = bottomAxisValueFormatter,
                guideline = null
            )
        ),
        model = model,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun FilterChipRow(selectedFilter: FilterRange, onFilterSelected: (FilterRange) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterRange.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            val bgColor by animateColorAsState(if (isSelected) ChipSelectedBg else ChipUnselectedBg, label = "")
            val textColor by animateColorAsState(if (isSelected) ChipSelectedText else ChipUnselectedText, label = "")
            Surface(onClick = { onFilterSelected(filter) }, shape = RoundedCornerShape(50), color = bgColor) {
                Text(
                    text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge.copy(color = textColor, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(modifier: Modifier = Modifier, bgColor: Color, label: String, value: String, unit: String) {
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(bgColor).padding(horizontal = 14.dp, vertical = 14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF5C5C7A), fontWeight = FontWeight.Medium))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold))
            Text(unit, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8888A8), fontSize = 10.sp))
        }
    }
}

@Composable
private fun ChartCard(content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(CardRadius)).background(Color.White).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, content = content)
}

@Composable
private fun PagerDots(currentPage: Int, initialPage: Int) {
    val offset = (currentPage - initialPage).coerceIn(-2, 2)
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        (-2..2).forEach { i ->
            val isActive = i == offset
            Box(modifier = Modifier.size(if (isActive) 8.dp else 5.dp).clip(CircleShape).background(if (isActive) HeroCardColor else Color(0xFFCCCCDD)))
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label — ${percentage.toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF5C5C7A), fontWeight = FontWeight.Medium))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getFormattedHeader(filter: FilterRange, offset: Int): String {
    val today = LocalDate.now()
    return when (filter) {
        FilterRange.WEEK -> {
            val targetWeek   = today.plusWeeks(offset.toLong())
            val startOfWeek  = targetWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek    = startOfWeek.plusDays(6)
            val fmt          = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
            val yearFmt      = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())
            "${startOfWeek.format(fmt)} – ${endOfWeek.format(fmt)}, ${endOfWeek.format(yearFmt)}"
        }
        FilterRange.MONTH -> {
            val targetMonth = today.plusMonths(offset.toLong())
            targetMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        }
        FilterRange.YEAR -> today.plusYears(offset.toLong()).year.toString()
    }
}