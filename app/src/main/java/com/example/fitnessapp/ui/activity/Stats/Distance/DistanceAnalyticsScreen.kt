package com.example.fitnessapp.ui.activity.Stats.Distance

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.Map
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
import com.example.fitnessapp.Data.Model.StatsDataClasses.DistanceSplitData
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
import com.example.fitnessapp.ui.theme.*


private val CardRadius        = 20.dp
private val SectionPadding    = 20.dp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DistanceAnalyticsScreen(navController: NavController) {
    val viewModel: AnalyticsViewModel = hiltViewModel()
    var selectedFilter by remember { mutableStateOf(FilterRange.WEEK) }
    val scope = rememberCoroutineScope()

    // 1. Collect the dynamic total pages from your ViewModel
    val totalPages by viewModel.pagerCount.collectAsState(initial = 1)

    // 2. Calculate the "Today" page (which is the very last index)
    val startPage = (totalPages - 1).coerceAtLeast(0)

    // 3. Set the Pager to use our exact boundaries
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { totalPages }
    )

    // Whenever the filter changes (Week -> Month), jump back to "Today"
    LaunchedEffect(selectedFilter, startPage) {
        pagerState.animateScrollToPage(startPage)
    }

    // Translate Compose's page index (0 to startPage) into your negative offsets (e.g., 0, -1, -2)
    val currentOffset = pagerState.currentPage - startPage

    val headerText = remember(selectedFilter, currentOffset) {
        getFormattedHeader(selectedFilter, currentOffset)
    }

    // Collect the UI state for Distance
    val currentUiState by viewModel
        .getDistanceDataForPage(selectedFilter, currentOffset)
        .collectAsState(initial = DistanceDataUiState())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
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
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ChipUnselectedBg)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = ChipUnselectedText,
                    modifier = Modifier.size(24.dp)
                )
            }

            // ── 1. Screen title ──────────────────────────────────────────
            Text(
                text = "Distance", // Updated Title
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
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
            if (currentUiState.totalDistance > 0f) {
                QuickStatsRow(uiState = currentUiState, selectedFilter)
            }

            // ── 5. Swipeable bar chart ───────────────────────────────────
            ChartCard {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) { page ->
                    val pageOffset = page - startPage // <-- Updated to startPage!
                    val uiState by viewModel
                        .getDistanceDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = DistanceDataUiState())

                    // CHECK FOR EMPTY STATE
                    if (uiState.totalDistance > 0f) {
                        DistanceBarChart(chartData = uiState.chartData)
                    } else {
                        // EMPTY STATE UI
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No runs logged",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFF8888A8),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Check back when you've hit the pavement.",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.LightGray
                                    )
                                )
                            }
                        }
                    }
                }

                // Page indicator dots
                Spacer(modifier = Modifier.height(12.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = startPage) // <-- Updated to startPage!
            }

            if (currentUiState.totalDistance > 0f) {
                DistanceSplitCard(splitData = currentUiState.distanceSplit)
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Filter Chip Row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FilterChipRow(
    selectedFilter: FilterRange,
    onFilterSelected: (FilterRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterRange.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) ChipSelectedBg else ChipUnselectedBg,
                animationSpec = tween(durationMillis = 200),
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) ChipSelectedText else ChipUnselectedText,
                animationSpec = tween(durationMillis = 200),
                label = "chipText"
            )

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(50),
                color = bgColor,
                shadowElevation = 0.dp
            ) {
                Text(
                    text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Hero Summary Card (Updated for Float and KM formatting)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroSummaryCard(
    headerText: String,
    uiState: DistanceDataUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Brush.linearGradient(colors = listOf(HeroCardColor, HeroCardAccent)))
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Rounded.ChevronLeft, "Previous", tint = OnHeroSubText, modifier = Modifier.size(20.dp))
                }

                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleLarge.copy(color = OnHeroSubText, fontWeight = FontWeight.Medium)
                )

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Rounded.ChevronRight, "Next", tint = OnHeroSubText, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Updated Icon and Text
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Map, // Map icon instead of Walk
                        contentDescription = null,
                        tint = OnHeroSubText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Daily Average",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnHeroSubText)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.chartData.isNotEmpty()) {
                // Formatting Float to 2 decimal places (e.g., 5.42)
                Text(
                    text = "%,.2f".format(uiState.dailyAverage),
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = OnHeroText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                )
                Text(
                    text = "km / day", // Updated unit string
                    style = MaterialTheme.typography.titleMedium.copy(color = OnHeroSubText)
                )
            } else {
                Box(
                    modifier = Modifier
                        .height(72.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {}
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Quick Stats Row (Updated for Float formatting and KM units)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickStatsRow(uiState: DistanceDataUiState, selectedFilter: FilterRange) {
    // Utilize the totalDistance from the UiState to avoid manual summing in UI
    val peakDay = uiState.chartData.maxByOrNull { it.value }

    val (peakLabel, activeLabel, activeUnit) = when (selectedFilter) {
        FilterRange.WEEK -> Triple("Peak day", "Active days", "days")
        FilterRange.MONTH -> Triple("Peak week", "Active weeks", "weeks")
        FilterRange.YEAR -> Triple("Peak month", "Active months", "months")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardGreen,
            label    = "Total",
            value    = "%,.2f".format(uiState.totalDistance), // Float Formatting
            unit     = "km"
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardOrange,
            label    = peakLabel,
            value    = peakDay?.displayLabel ?: "–",
            unit     = "%,.2f km".format(peakDay?.value ?: 0f) // Float Formatting
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardTeal,
            label    = activeLabel,
            value    = uiState.chartData.count { it.value > 0f }.toString(), // Compare against Float
            unit     = activeUnit
        )
    }
}

@Composable
private fun MiniStatCard(modifier: Modifier = Modifier, bgColor: Color, label: String, value: String, unit: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF5C5C7A), fontWeight = FontWeight.Medium))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold))
            Text(unit, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8888A8), fontSize = 10.sp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chart Card & Pager Dots container
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ChartCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(CardRadius)).background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun PagerDots(currentPage: Int, initialPage: Int) {
    val offset = (currentPage - initialPage).coerceIn(-2, 2)
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        (-2..2).forEach { i ->
            val isActive = i == offset
            Box(
                modifier = Modifier
                    .size(if (isActive) 8.dp else 5.dp)
                    .clip(CircleShape)
                    .background(if (isActive) HeroCardColor else Color(0xFFCCCCDD))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bar Chart (Works seamlessly with Floats)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DistanceBarChart(chartData: List<ChartData>) {
    val bottomAxisValueFormatter = CartesianValueFormatter { x, _, _ ->
        chartData.getOrNull(x.toInt())?.displayLabel ?: ""
    }

    val model = CartesianChartModel(
        ColumnCartesianLayerModel.build {
            series(chartData.map { it.value }) // Vico natively handles Floats
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(color = HeroCardColor, thickness = 12.dp, shape = remember { Shape.rounded(allPercent = 25) })
                )
            ),
            startAxis = rememberStartAxis(
                label = rememberAxisLabelComponent(color = ChipUnselectedText),
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
fun DistanceSplitCard(splitData: DistanceSplitData) {
    val hasData = splitData.lessThan5k > 0f || splitData.fiveTo10k > 0f || splitData.moreThan10k > 0f

    // The dark background color from your image
    val DarkCardBackground = Color(0xFFFFFFFF)
    val LightText = Color.Black
    val MutedText = Color(0xFF8888A8)

    val ColorLessThan5k = Color(0xFF2ECC71)   // Green
    val Color5To10k = Color(0xFF3B82F6)       // Blue
    val ColorMoreThan10k = Color(0xFFF59E0B)  // Orange

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCardBackground) // Changed to dark
            .padding(20.dp)
    ) {
        // 1. Titles
        Text(
            text = "Distance split",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = LightText // Changed to white
            )
        )
        Text(
            text = "by run length",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MutedText
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
                    color = Color.DarkGray.copy(alpha = 0.3f), // Adjusted for dark mode
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke
                )
            } else {
                val sweep1 = (splitData.lessThan5k / 100f) * 360f
                if (sweep1 > 0) {
                    drawArc(color = ColorLessThan5k, startAngle = startAngle, sweepAngle = sweep1, useCenter = false, style = stroke)
                    startAngle += sweep1
                }

                val sweep2 = (splitData.fiveTo10k / 100f) * 360f
                if (sweep2 > 0) {
                    drawArc(color = Color5To10k, startAngle = startAngle, sweepAngle = sweep2, useCenter = false, style = stroke)
                    startAngle += sweep2
                }

                val sweep3 = (splitData.moreThan10k / 100f) * 360f
                if (sweep3 > 0) {
                    drawArc(color = ColorMoreThan10k, startAngle = startAngle, sweepAngle = sweep3, useCenter = false, style = stroke)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Legend
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LegendItem(color = ColorLessThan5k, label = "< 5 km", percentage = splitData.lessThan5k)
            LegendItem(color = Color5To10k, label = "5 – 10 km", percentage = splitData.fiveTo10k)
            LegendItem(color = ColorMoreThan10k, label = "> 10 km", percentage = splitData.moreThan10k)
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
                color = Color(0xFF9494C9), // Changed to lighter gray for visibility
                fontWeight = FontWeight.Medium
            )
        )
    }
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