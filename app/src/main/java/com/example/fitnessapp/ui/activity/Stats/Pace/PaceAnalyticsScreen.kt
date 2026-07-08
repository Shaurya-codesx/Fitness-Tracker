package com.example.fitnessapp.ui.activity.Stats.Pace

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
import com.example.fitnessapp.R
import com.example.fitnessapp.ui.theme.*


private val CardRadius = 24.dp
private val SectionPadding = 20.dp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaceAnalyticsScreen(navController: NavController) {
    val viewModel: AnalyticsViewModel = hiltViewModel()
    var selectedFilter by remember { mutableStateOf(FilterRange.WEEK) }
    val scope = rememberCoroutineScope()

    val totalPages by viewModel.pagerCount.collectAsState(initial = 1)
    val startPage = (totalPages - 1).coerceAtLeast(0)

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { totalPages }
    )

    LaunchedEffect(selectedFilter, startPage) {
        pagerState.animateScrollToPage(startPage)
    }

    val currentOffset = pagerState.currentPage - startPage
    val headerText = remember(selectedFilter, currentOffset) {
        getFormattedHeader(selectedFilter, currentOffset)
    }

    val currentUiState by viewModel
        .getPaceDataForPage(selectedFilter, currentOffset)
        .collectAsState(initial = PaceDataUiState())

    Box(
        modifier = Modifier.fillMaxSize().background(PaceBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SectionPadding)
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // ── Inline top bar ──────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape, ambientColor = PaceChipSelectedBg.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = PaceChipUnselectedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.pace_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PaceTextPrimary
                    )
                )
            }

            // ── Filter chips ──────────────────────────────────────────
            FilterChipRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // ── Hero summary card ─────────────────────────────────────
            HeroSummaryCard(
                headerText = headerText,
                uiState = currentUiState,
                onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext    = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            )

            // ── Bento stat row ──────────────────────────────────────
            if (currentUiState.averagePaceDecimal > 0f) {
                QuickStatsRow(uiState = currentUiState, selectedFilter)
            }

            // ── Chart card ────────────────────────────────────────────
            ChartCard(headerText = headerText) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                ) { page ->
                    val pageOffset = page - startPage
                    val uiState by viewModel
                        .getPaceDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = PaceDataUiState())

                    if (uiState.averagePaceDecimal > 0f) {
                        PaceLineChart(chartData = uiState.chartData)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.pace_no_data_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = PaceTextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.pace_no_data_subtitle),
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color.LightGray)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = startPage)
            }

            if (currentUiState.averagePaceDecimal > 0f) {
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
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) PaceChipSelectedBg else PaceChipUnselectedBg,
                animationSpec = tween(200), label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) PaceChipSelectedText else PaceChipUnselectedText,
                animationSpec = tween(200), label = "chipText"
            )

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(50),
                color = bgColor,
                shadowElevation = if (isSelected) 4.dp else 0.dp
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
// Hero Summary Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroSummaryCard(
    headerText: String,
    uiState: PaceDataUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(CardRadius), ambientColor = PaceHeroPrimary.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(CardRadius))
            .background(Brush.verticalGradient(listOf(PaceHeroPrimary, PaceHeroAccent)))
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                style = MaterialTheme.typography.labelLarge.copy(color = PaceOnHeroSubText, fontWeight = FontWeight.Medium)
            )
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = stringResource(R.string.content_desc_previous),
                        tint = PaceOnHeroText,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.25f)))
                IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.content_desc_next),
                        tint = PaceOnHeroText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            if (uiState.chartData.any { it.value > 0f }) {
                Column {
                    Text(
                        text = stringResource(R.string.pace_average_pace),
                        style = MaterialTheme.typography.labelMedium.copy(color = PaceOnHeroSubText)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = PaceFormatterUseCase.formatDecimalPaceToString(uiState.averagePaceDecimal),
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = PaceOnHeroText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 52.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.pace_min_per_km),
                            style = MaterialTheme.typography.titleSmall.copy(color = PaceOnHeroSubText),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = PaceOnHeroText,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(72.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {}
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Quick Stats Row — bento asymmetric layout
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickStatsRow(uiState: PaceDataUiState, selectedFilter: FilterRange) {
    val bestDay = uiState.chartData.filter { it.value > 0f }.minByOrNull { it.value }

    val peakLabel = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.pace_peak_label_week)
        FilterRange.MONTH -> stringResource(R.string.pace_peak_label_month)
        FilterRange.YEAR -> stringResource(R.string.pace_peak_label_year)
    }
    val activeLabel = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.pace_active_label_week)
        FilterRange.MONTH -> stringResource(R.string.pace_active_label_month)
        FilterRange.YEAR -> stringResource(R.string.pace_active_label_year)
    }
    val activeUnit = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.pace_active_unit_days)
        FilterRange.MONTH -> stringResource(R.string.pace_active_unit_weeks)
        FilterRange.YEAR -> stringResource(R.string.pace_active_unit_months)
    }
    val minPerKmSuffix = stringResource(R.string.pace_min_per_km_suffix)

    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wide hero stat — average pace
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = PaceStatCardPrimary.copy(alpha = 0.8f))
                .clip(RoundedCornerShape(20.dp))
                .background(PaceStatCardPrimary)
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Timer, null, tint = PaceHeroAccent, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        PaceFormatterUseCase.formatDecimalPaceToString(uiState.averagePaceDecimal),
                        style = MaterialTheme.typography.headlineMedium.copy(color = PaceTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                    )
                    Text(
                        stringResource(R.string.pace_avg_pace_label) + " (" + stringResource(R.string.pace_min_per_km_short) + ")",
                        style = MaterialTheme.typography.labelSmall.copy(color = PaceMutedText)
                    )
                }
            }
        }

        // Right column: two stacked compact stats
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompactStatCard(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                bgColor = PaceStatCardSecondary,
                icon = Icons.Rounded.Bolt,
                iconTint = Color(0xFF9A6B2E),
                label = peakLabel,
                value = bestDay?.displayLabel ?: "–"
            )
            CompactStatCard(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                bgColor = PaceStatCardTertiary,
                icon = Icons.Rounded.CalendarMonth,
                iconTint = Color(0xFF2E6E7A),
                label = activeLabel,
                value = "${uiState.chartData.count { it.value > 0f }} $activeUnit"
            )
        }
    }
}

@Composable
private fun CompactStatCard(
    modifier: Modifier = Modifier,
    bgColor: Color,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = bgColor.copy(alpha = 0.8f))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(15.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleSmall.copy(color = PaceTextPrimary, fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = PaceMutedText, fontSize = 10.sp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chart Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ChartCard(headerText: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(CardRadius), ambientColor = PaceHeroPrimary.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(CardRadius))
            .background(Color.White)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.pace_chart_card_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PaceTextPrimary)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PaceHeroPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(headerText, style = MaterialTheme.typography.labelSmall.copy(color = PaceHeroPrimary, fontWeight = FontWeight.SemiBold))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
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
                    .background(if (isActive) PaceHeroPrimary else Color(0xFFCCCCDD))
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Line Chart — thicker line + bigger points for visibility
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PaceLineChart(chartData: List<ChartData>) {

    val seriesData = PaceFormatterUseCase.preparePaceChartSeries(chartData)
    val xValues = chartData.indices.map { it.toFloat() }

    val model = CartesianChartModel(
        listOf(
            LineCartesianLayerModel.build {
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
                lines = listOf(
                    rememberLineSpec(
                        point = rememberShapeComponent(
                            shape = Shape.Pill,
                            color = PaceHeroPrimary
                        ),
                        pointSize = 14.dp, // was 8.dp — bigger, more noticeable
                        thickness = 4.dp   // explicit thicker line stroke
                    )
                )
            ),
            startAxis = rememberStartAxis(
                label = rememberAxisLabelComponent(color = PaceChipUnselectedText),
                valueFormatter = startAxisValueFormatter,
                guideline = rememberAxisGuidelineComponent(color = Color.LightGray.copy(alpha = 0.5f))
            ),
            bottomAxis = rememberBottomAxis(
                label = rememberAxisLabelComponent(color = PaceChipUnselectedText),
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
            val targetWeek = today.plusWeeks(offset.toLong())
            val startOfWeek = targetWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek = startOfWeek.plusDays(6)
            val fmt = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
            val yearFmt = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())
            "${startOfWeek.format(fmt)} – ${endOfWeek.format(fmt)}, ${endOfWeek.format(yearFmt)}"
        }
        FilterRange.MONTH -> today.plusMonths(offset.toLong()).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        FilterRange.YEAR -> today.plusYears(offset.toLong()).year.toString()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Pace Split — side-by-side donut + legend
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PaceSplitCard(splitData: PaceSplitData) {
    val hasData = splitData.walkingPct > 0f || splitData.joggingPct > 0f || splitData.runningPct > 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = PaceHeroPrimary.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.pace_split_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PaceTextPrimary)
        )
        Text(
            text = stringResource(R.string.pace_split_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = PaceTextSecondary)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 26.dp.toPx()
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                var startAngle = -90f

                if (!hasData) {
                    drawArc(color = Color.LightGray.copy(alpha = 0.3f), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
                } else {
                    val sweep1 = (splitData.walkingPct / 100f) * 360f
                    if (sweep1 > 0) {
                        drawArc(color = PaceDonutWalking, startAngle = startAngle, sweepAngle = sweep1, useCenter = false, style = stroke)
                        startAngle += sweep1
                    }
                    val sweep2 = (splitData.joggingPct / 100f) * 360f
                    if (sweep2 > 0) {
                        drawArc(color = PaceDonutJogging, startAngle = startAngle, sweepAngle = sweep2, useCenter = false, style = stroke)
                        startAngle += sweep2
                    }
                    val sweep3 = (splitData.runningPct / 100f) * 360f
                    if (sweep3 > 0) {
                        drawArc(color = PaceDonutRunning, startAngle = startAngle, sweepAngle = sweep3, useCenter = false, style = stroke)
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                LegendItem(color = PaceDonutWalking, label = stringResource(R.string.pace_legend_walking), percentage = splitData.walkingPct)
                LegendItem(color = PaceDonutJogging, label = stringResource(R.string.pace_legend_jogging), percentage = splitData.joggingPct)
                LegendItem(color = PaceDonutRunning, label = stringResource(R.string.pace_legend_running), percentage = splitData.runningPct)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = PaceMutedText, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(color = PaceTextPrimary, fontWeight = FontWeight.Bold)
        )
    }
}