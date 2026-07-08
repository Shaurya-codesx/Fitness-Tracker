package com.example.fitnessapp.ui.activity.Stats.Steps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsWalk
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
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.fitnessapp.ui.activity.Stats.AnalyticsViewModel
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
import kotlinx.coroutines.launch
import java.time.temporal.TemporalAdjusters
import kotlinx.datetime.DayOfWeek
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.R


private val CardRadius = 24.dp
private val SectionPadding = 20.dp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepsAnalyticsScreen(navController: NavController) {
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
        .getStepsDataForPage(selectedFilter, currentOffset)
        .collectAsState(initial = StepsDataUiState())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StepsBackground)
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
                        .shadow(4.dp, CircleShape, ambientColor = StepsChipSelectedBg.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = StepsChipUnselectedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.steps_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = StepsTextPrimary
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
            if (currentUiState.totalSteps > 0) {
                QuickStatsRow(uiState = currentUiState, selectedFilter)
            }

            // ── Chart card ────────────────────────────────────────────
            ChartCard(headerText = headerText) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) { page ->
                    val pageOffset = page - startPage
                    val uiState by viewModel
                        .getStepsDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = StepsDataUiState())

                    if (uiState.totalSteps > 0) {
                        StepsBarChart(chartData = uiState.chartData)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.steps_no_data_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = StepsTextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.steps_no_data_subtitle),
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color.LightGray)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = startPage)
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
                targetValue = if (isSelected) StepsChipSelectedBg else StepsChipUnselectedBg,
                animationSpec = tween(durationMillis = 200),
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) StepsChipSelectedText else StepsChipUnselectedText,
                animationSpec = tween(durationMillis = 200),
                label = "chipText"
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
    uiState: StepsDataUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(CardRadius), ambientColor = StepsHeroPrimary.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(CardRadius))
            .background(Brush.verticalGradient(listOf(StepsHeroPrimary, StepsHeroAccent)))
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                style = MaterialTheme.typography.labelLarge.copy(color = StepsOnHeroSubText, fontWeight = FontWeight.Medium)
            )
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = stringResource(R.string.content_desc_previous),
                        tint = StepsOnHeroText,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.25f)))
                IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.content_desc_next),
                        tint = StepsOnHeroText,
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
            if (uiState.chartData.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(R.string.steps_daily_average),
                        style = MaterialTheme.typography.labelMedium.copy(color = StepsOnHeroSubText)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%,d".format(uiState.dailyAverage),
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = StepsOnHeroText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 52.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.steps_per_day),
                            style = MaterialTheme.typography.titleSmall.copy(color = StepsOnHeroSubText),
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
                        imageVector = Icons.Rounded.DirectionsWalk,
                        contentDescription = null,
                        tint = StepsOnHeroText,
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
private fun QuickStatsRow(uiState: StepsDataUiState, selectedFilter: FilterRange) {
    val totalSteps = uiState.chartData.sumOf { it.value }
    val peakDay = uiState.chartData.maxByOrNull { it.value }

    val peakLabel = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.steps_peak_label_week)
        FilterRange.MONTH -> stringResource(R.string.steps_peak_label_month)
        FilterRange.YEAR -> stringResource(R.string.steps_peak_label_year)
    }
    val activeLabel = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.steps_active_label_week)
        FilterRange.MONTH -> stringResource(R.string.steps_active_label_month)
        FilterRange.YEAR -> stringResource(R.string.steps_active_label_year)
    }
    val activeUnit = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.steps_active_unit_days)
        FilterRange.MONTH -> stringResource(R.string.steps_active_unit_weeks)
        FilterRange.YEAR -> stringResource(R.string.steps_active_unit_months)
    }
    val stepsUnit = stringResource(R.string.steps_unit_short)

    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wide hero stat — total steps
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = StepsStatCardPrimary.copy(alpha = 0.8f))
                .clip(RoundedCornerShape(20.dp))
                .background(StepsStatCardPrimary)
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
                    Icon(Icons.Rounded.DirectionsWalk, null, tint = StepsHeroAccent, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        "%,d".format(totalSteps),
                        style = MaterialTheme.typography.headlineMedium.copy(color = StepsTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                    )
                    Text(
                        stringResource(R.string.steps_total_label) + " ($stepsUnit)",
                        style = MaterialTheme.typography.labelSmall.copy(color = StepsMutedText)
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
                bgColor = StepsStatCardSecondary,
                icon = Icons.Rounded.Bolt,
                iconTint = Color(0xFF9A6B2E),
                label = peakLabel,
                value = peakDay?.displayLabel ?: "–"
            )
            CompactStatCard(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                bgColor = StepsStatCardTertiary,
                icon = Icons.Rounded.CalendarMonth,
                iconTint = Color(0xFF2E6E7A),
                label = activeLabel,
                value = "${uiState.chartData.count { it.value > 0 }} $activeUnit"
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
            Text(value, style = MaterialTheme.typography.titleSmall.copy(color = StepsTextPrimary, fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = StepsMutedText, fontSize = 10.sp))
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
            .shadow(8.dp, RoundedCornerShape(CardRadius), ambientColor = StepsHeroPrimary.copy(alpha = 0.18f))
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
                stringResource(R.string.steps_chart_card_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = StepsTextPrimary)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StepsHeroPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(headerText, style = MaterialTheme.typography.labelSmall.copy(color = StepsHeroPrimary, fontWeight = FontWeight.SemiBold))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Pager dots indicator
// ─────────────────────────────────────────────────────────────────────────────
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
                    .background(if (isActive) StepsHeroPrimary else Color(0xFFCCCCDD))
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Bar Chart — thicker bars, matches Distance/Energy thickness
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StepsBarChart(chartData: List<ChartData>) {
    val bottomAxisValueFormatter = CartesianValueFormatter { x, _, _ ->
        chartData.getOrNull(x.toInt())?.displayLabel ?: ""
    }

    val model = CartesianChartModel(
        ColumnCartesianLayerModel.build {
            series(chartData.map { it.value })
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        color = StepsHeroPrimary,
                        thickness = 22.dp, // matches Distance/Energy screens
                        shape = remember { Shape.rounded(allPercent = 35) }
                    )
                )
            ),
            startAxis = rememberStartAxis(
                label = rememberAxisLabelComponent(color = StepsChipUnselectedText),
                guideline = rememberAxisGuidelineComponent(color = Color.LightGray.copy(alpha = 0.5f))
            ),
            bottomAxis = rememberBottomAxis(
                label = rememberAxisLabelComponent(color = StepsChipUnselectedText),
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