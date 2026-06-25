package com.example.fitnessapp.ui.activity.Stats.Pace

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fitnessapp.Domain.UseCases.PaceFormatterUseCase
import com.example.fitnessapp.ui.activity.Stats.AnalyticsViewModel
import com.example.fitnessapp.ui.activity.Stats.FilterRange

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.fitnessapp.Data.Model.StatsDataClasses.PaceSplitData

import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlinx.datetime.DayOfWeek

// ─────────────────────────────────────────────
// Design Tokens – Kept identical for UI consistency
// ─────────────────────────────────────────────
private val BackgroundColor   = Color(0xFFF2F1F8)
private val HeroCardColor     = Color(0xFF82D497)
private val HeroCardAccent    = Color(0xFFE0F0E8)

private val ChipSelectedBg    = Color(0xFF3B5A8A)
private val ChipSelectedText  = Color(0xFFFFFFFF)
private val ChipUnselectedBg  = Color(0xFFE8E6F0)
private val ChipUnselectedText= Color(0xFF5C5C7A)

private val StatCardGreen     = Color(0xFFDFF2E1)
private val StatCardOrange    = Color(0xFFFDEDD8)
private val StatCardTeal      = Color(0xFFD6F2EF)

private val OnHeroText        = Color(0xFFFFFFFF)
private val OnHeroSubText     = Color(0xFF337B4B)

private val CardRadius        = 20.dp
private val SectionPadding    = 20.dp


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaceAnalyticsScreen(navController: NavController) {
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

    // Collect the UI state for Pace
    val currentUiState by viewModel
        .getPaceDataForPage(selectedFilter, currentOffset)
        .collectAsState(initial = PaceDataUiState())

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
                text = "Pace",
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

            // ── 4. Quick stat row below the hero ─────────────────────────
            if (currentUiState.chartData.isNotEmpty()) {
                QuickStatsRow(uiState = currentUiState, selectedFilter = selectedFilter)
            }

            // ── 5. Swipeable LINE chart ───────────────────────────────────
            ChartCard {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                ) { page ->
                    val pageOffset = page - initialPage
                    val uiState by viewModel
                        .getPaceDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = PaceDataUiState())

                    // We only render if there's at least one valid run in this period
                    if (uiState.chartData.any { it.value > 0f }) {
                        PaceLineChart(chartData = uiState.chartData)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No runs logged",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ChipUnselectedText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = initialPage)
            }

            if (currentUiState.chartData.any { it.value > 0f }) {
                PaceSplitCard(splitData = currentUiState.paceSplit)
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Filter Chip Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FilterChipRow(selectedFilter: FilterRange, onFilterSelected: (FilterRange) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterRange.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            val bgColor by animateColorAsState(if (isSelected) ChipSelectedBg else ChipUnselectedBg, label = "chipBg")
            val textColor by animateColorAsState(if (isSelected) ChipSelectedText else ChipUnselectedText, label = "chipText")

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(50),
                color = bgColor
            ) {
                Text(
                    text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge.copy(color = textColor, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Hero Summary Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroSummaryCard(headerText: String, uiState: PaceDataUiState, onPrevious: () -> Unit, onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Brush.linearGradient(colors = listOf(HeroCardColor, HeroCardAccent)))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))) {
                    Icon(Icons.Rounded.ChevronLeft, "Previous", tint = OnHeroSubText, modifier = Modifier.size(20.dp))
                }
                Text(headerText, style = MaterialTheme.typography.titleLarge.copy(color = OnHeroSubText, fontWeight = FontWeight.Medium))
                IconButton(onClick = onNext, modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))) {
                    Icon(Icons.Rounded.ChevronRight, "Next", tint = OnHeroSubText, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = OnHeroSubText, modifier = Modifier.size(14.dp))
                    Text("Average Pace", style = MaterialTheme.typography.labelSmall.copy(color = OnHeroSubText))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.chartData.any { it.value > 0f }) {
                Text(
                    text = PaceFormatterUseCase.formatDecimalPaceToString(uiState.averagePaceDecimal), // Use our formatter!
                    style = MaterialTheme.typography.displaySmall.copy(color = OnHeroText, fontWeight = FontWeight.Bold, fontSize = 48.sp)
                )
                Text("min / km", style = MaterialTheme.typography.titleMedium.copy(color = OnHeroSubText))
            } else {
                Box(modifier = Modifier.height(72.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.1f))) {}
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Quick Stats Row (Pace Focus)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickStatsRow(uiState: PaceDataUiState, selectedFilter: FilterRange) {
    // For Pace, the "best" day is the LOWEST value that is greater than 0
    val bestDay = uiState.chartData.filter { it.value > 0f }.minByOrNull { it.value }

    val (peakLabel, activeLabel, activeUnit) = when (selectedFilter) {
        FilterRange.WEEK -> Triple("Fastest day", "Active days", "days")
        FilterRange.MONTH -> Triple("Fastest week", "Active weeks", "weeks")
        FilterRange.YEAR -> Triple("Fastest month", "Active months", "months")
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardGreen,
            label    = "Avg Pace",
            value    = PaceFormatterUseCase.formatDecimalPaceToString(uiState.averagePaceDecimal),
            unit     = "min/km"
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardOrange,
            label    = peakLabel,
            value    = bestDay?.displayLabel ?: "–",
            unit     = PaceFormatterUseCase.formatDecimalPaceToString(bestDay?.value ?: 0f) + " /km"
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


// ─────────────────────────────────────────────────────────────────────────────
// Chart Card & Pager Dots
// ─────────────────────────────────────────────────────────────────────────────
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


// ─────────────────────────────────────────────────────────────────────────────
// Line Chart (Vico 2.0 LineCartesianLayer)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PaceLineChart(chartData: List<ChartData>) {

    val seriesData = PaceFormatterUseCase.preparePaceChartSeries(chartData)

    // 1. Explicitly define X-axis coordinates so Vico doesn't trim empty months
    val xValues = chartData.indices.map { it.toFloat() }

    val model = CartesianChartModel(
        listOf(
            LineCartesianLayerModel.build {
                // We pair X and Y, filter out points where Y is null, then unzip back to two lists
                val paired = xValues.zip(seriesData).filter { it.second != null }
                if (paired.isNotEmpty()) {
                    series(
                        x = paired.map { it.first },
                        y = paired.map { it.second!! }
                    )
                }
            }
        )
    )

    val bottomAxisValueFormatter = CartesianValueFormatter { x, _, _ ->
        chartData.getOrNull(x.toInt())?.displayLabel ?: ""
    }

    val startAxisValueFormatter = CartesianValueFormatter { y, _, _ ->
        PaceFormatterUseCase.formatDecimalPaceToString(kotlin.math.abs(y.toFloat()))
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                // 3. Configure the line to show dots (points) so single runs are visible!
                lines = listOf(
                    rememberLineSpec(
                        point = rememberShapeComponent(
                            shape = Shape.Pill,
                            color = HeroCardColor
                        ),
                        pointSize = 8.dp
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

// ─────────────────────────────────────────────────────────────────────────────
// Date header helper
// ─────────────────────────────────────────────────────────────────────────────
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
        FilterRange.YEAR -> {
            today.plusYears(offset.toLong()).year.toString()
        }
    }
}


@Composable
fun PaceSplitCard(splitData: PaceSplitData) {
    val hasData = splitData.walkingPct > 0f || splitData.joggingPct > 0f || splitData.runningPct > 0f

    // Updated colors to match the light theme of the screen
    val CardBackground = Color.White
    val TitleTextColor = Color(0xFF1A1A2E)
    val SubTextColor   = ChipUnselectedText

    // Theme-consistent colors for the chart zones
    val ColorWalking = Color(0xFFFFB267) // Vibrant orange to match StatCardOrange theme
    val ColorJogging = ChipSelectedBg    // Theme Blue
    val ColorRunning = HeroCardColor     // Theme Green

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(CardBackground)
            .padding(20.dp)
    ) {
        // 1. Titles
        Text(
            text = "Pace split",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TitleTextColor
            )
        )
        Text(
            text = "by pace zone",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = SubTextColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Native Canvas Donut Chart
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val strokeWidth = 35.dp.toPx()
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            var startAngle = -90f

            if (!hasData) {
                drawArc(
                    color = ChipUnselectedBg,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
            } else {
                val sweep1 = (splitData.walkingPct / 100f) * 360f
                if (sweep1 > 0) {
                    drawArc(color = ColorWalking, startAngle = startAngle, sweepAngle = sweep1, useCenter = false, style = stroke)
                    startAngle += sweep1
                }

                val sweep2 = (splitData.joggingPct / 100f) * 360f
                if (sweep2 > 0) {
                    drawArc(color = ColorJogging, startAngle = startAngle, sweepAngle = sweep2, useCenter = false, style = stroke)
                    startAngle += sweep2
                }

                val sweep3 = (splitData.runningPct / 100f) * 360f
                if (sweep3 > 0) {
                    drawArc(color = ColorRunning, startAngle = startAngle, sweepAngle = sweep3, useCenter = false, style = stroke)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Legend
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendItem(color = ColorWalking, label = "Walking (> 8:00/km)", percentage = splitData.walkingPct)
            LegendItem(color = ColorJogging, label = "Jogging (6:00 - 8:00)", percentage = splitData.joggingPct)
            LegendItem(color = ColorRunning, label = "Running (< 6:00/km)", percentage = splitData.runningPct)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$label — ${percentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ChipUnselectedText,
                fontWeight = FontWeight.Medium
            )
        )
    }
}