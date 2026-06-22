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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowLeft
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.navigation.NavController
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

// ─────────────────────────────────────────────
// Design Tokens – match your app's visual DNA
// ─────────────────────────────────────────────
private val BackgroundColor   = Color(0xFFF2F1F8) // near-white lavender
private val HeroCardColor     = Color(0xFF3B5A8A) // muted dark blue (hero card)
private val HeroCardAccent    = Color(0xFF4F7AB3) // lighter blue for gradient end

private val ChipSelectedBg    = Color(0xFF3B5A8A)
private val ChipSelectedText  = Color(0xFFFFFFFF)
private val ChipUnselectedBg  = Color(0xFFE8E6F0)
private val ChipUnselectedText= Color(0xFF5C5C7A)

private val StatCardGreen     = Color(0xFFDFF2E1) // soft pastel green
private val StatCardOrange    = Color(0xFFFDEDD8) // soft pastel orange
private val StatCardTeal      = Color(0xFFD6F2EF) // soft pastel teal

private val OnHeroText        = Color(0xFFFFFFFF)
private val OnHeroSubText     = Color(0xFFB8CEDE)

private val CardRadius        = 20.dp
private val SectionPadding    = 20.dp


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepsAnalyticsScreen(navController: NavController) {
    val viewModel: StepsViewModel = hiltViewModel()
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

    // Collect the UI state for the current page to show summary stats above the pager
    val currentUiState by viewModel
        .getStepsDataForPage(selectedFilter, currentOffset)
        .collectAsState(initial = StepsDataUiState())

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
                    .background(ChipUnselectedBg) // Using existing light gray token
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
                text = "Steps",
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

            // ── 3. Hero summary card (dark blue, full width) ─────────────
            HeroSummaryCard(
                headerText = headerText,
                uiState = currentUiState,
                onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext    = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            )

            // ── 4. Quick stat row below the hero ─────────────────────────
            if (currentUiState.chartData.isNotEmpty()) {
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
                    val pageOffset = page - initialPage
                    val uiState by viewModel
                        .getStepsDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = StepsDataUiState())

                    if (uiState.chartData.isNotEmpty()) {
                        StepsBarChart(chartData = uiState.chartData)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = HeroCardColor,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                // Page indicator dots
                Spacer(modifier = Modifier.height(12.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = initialPage)
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
                    text = filter.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
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
// Hero Summary Card  (dark blue gradient, mirrors the 0.2 KM card in the app)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSummaryCard(
    headerText: String,
    uiState: StepsDataUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(HeroCardColor, HeroCardAccent)
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Date navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous",
                        tint = OnHeroSubText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = OnHeroSubText,
                        fontWeight = FontWeight.Medium
                    )
                )

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Next",
                        tint = OnHeroSubText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Walk icon pill
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
                        imageVector = Icons.Rounded.DirectionsWalk,
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
                Text(
                    text = "%,d".format(uiState.dailyAverage),
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = OnHeroText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                )
                Text(
                    text = "steps / day",
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
// Quick Stats Row  (pastel cards matching the Performance grid in the app)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickStatsRow(uiState: StepsDataUiState, selectedFilter: FilterRange) {
    val totalSteps = uiState.chartData.sumOf { it.value }
    val peakDay    = uiState.chartData.maxByOrNull { it.value }

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
            value    = "%,d".format(totalSteps),
            unit     = "steps"
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardOrange,
            label    = peakLabel,
            value    = peakDay?.displayLabel ?: "–",
            unit     = "%,d steps".format(peakDay?.value ?: 0)
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            bgColor  = StatCardTeal,
            label    = activeLabel,
            value    = uiState.chartData.count { it.value > 0 }.toString(),
            unit     = activeUnit
        )
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    bgColor: Color,
    label: String,
    value: String,
    unit: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF5C5C7A),
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text  = unit,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF8888A8),
                    fontSize = 10.sp
                )
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Chart Card container
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChartCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
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
                    .background(
                        if (isActive) HeroCardColor
                        else Color(0xFFCCCCDD)
                    )
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Bar Chart  (unchanged logic, styled via Vico column layer)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StepsBarChart(chartData: List<ChartData>) {
    // 1. Define the labels for the X-Axis using your data's displayLabel
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
                        color = HeroCardColor,
                        thickness = 12.dp,
                        shape = remember { Shape.rounded(allPercent = 25) }
                    )
                )
            ),
            startAxis = rememberStartAxis(
                label = rememberAxisLabelComponent(color = ChipUnselectedText),
                guideline = rememberAxisGuidelineComponent(color = Color.LightGray.copy(alpha = 0.5f))
            ),
            bottomAxis = rememberBottomAxis(
                label = rememberAxisLabelComponent(color = ChipUnselectedText),
                // 2. Add the valueFormatter here to bring back Days/Weeks/Months labels
                valueFormatter = bottomAxisValueFormatter,
                guideline = null
            )
        ),
        model = model,
        modifier = Modifier.fillMaxSize()
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// Date header helper  (unchanged logic)
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