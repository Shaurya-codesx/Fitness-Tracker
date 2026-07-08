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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
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
import com.example.fitnessapp.R
// ─── Palette ───
private val BackgroundColor = Color(0xFFF7F5FC)
private val HeroCardColor = Color(0xFF6D93BD)
private val HeroCardAccent = Color(0xFF4F7195)
private val OnHeroText = Color.White
private val OnHeroSubText = Color.White.copy(alpha = 0.85f)

private val ChipSelectedBg = Color(0xFF6C63B5)
private val ChipUnselectedBg = Color.White
private val ChipSelectedText = Color.White
private val ChipUnselectedText = Color(0xFF8A8599)

private val StatCardGreen = Color(0xFFDCEEE0)
private val StatCardOrange = Color(0xFFF3E3D3)
private val StatCardTeal = Color(0xFFDCEBEE)

private val HeroGradient = Brush.verticalGradient(colors = listOf(HeroCardColor, HeroCardAccent))

private val CardRadius        = 24.dp
private val SectionPadding    = 20.dp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DistanceAnalyticsScreen(navController: NavController) {
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
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            // ── Inline top bar: back button + title in one row ──────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape, ambientColor = ChipSelectedBg.copy(alpha = 0.2f))
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = ChipUnselectedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.distance),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2D2A3D)
                    )
                )
            }

            // ── Filter chips ──────────────────────────────────────────
            FilterChipRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // ── Hero summary card — restructured layout ─────────────────
            HeroSummaryCard(
                headerText = headerText,
                uiState = currentUiState,
                onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext    = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
            )

            // ── Bento-style asymmetric stat row ──────────────────────────
            if (currentUiState.totalDistance > 0f) {
                QuickStatsRow(uiState = currentUiState, selectedFilter)
            }

            // ── Chart card with inline header ────────────────────────────
            ChartCard(headerText = headerText) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) { page ->
                    val pageOffset = page - startPage
                    val uiState by viewModel
                        .getDistanceDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = DistanceDataUiState())

                    if (uiState.totalDistance > 0f) {
                        DistanceBarChart(chartData = uiState.chartData)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.no_runs_logged_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFF8888A8),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.no_runs_logged_subtitle),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.LightGray
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = startPage)
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
// Hero Summary Card — REDESIGNED: number is now the focal point,
// header text + pager controls demoted to a slim top row,
// a small inline sparkline-style trend indicator added
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroSummaryCard(
    headerText: String,
    uiState: DistanceDataUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(CardRadius), ambientColor = HeroCardColor.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(CardRadius))
            .background(HeroGradient)
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        // Slim top row: date range + compact pager, de-emphasized
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = OnHeroSubText,
                    fontWeight = FontWeight.Medium
                )
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.ChevronLeft, "Previous", tint = OnHeroText, modifier = Modifier.size(16.dp))
                }
                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.25f)))
                IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.ChevronRight, "Next", tint = OnHeroText, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main content: big number on left, icon badge on right — split layout instead of centered stack
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            if (uiState.chartData.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(R.string.daily_avg),
                        style = MaterialTheme.typography.labelMedium.copy(color = OnHeroSubText)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%,.2f".format(uiState.dailyAverage),
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = OnHeroText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 52.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.km_per_day),
                            style = MaterialTheme.typography.titleSmall.copy(color = OnHeroSubText),
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
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        tint = OnHeroText,
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
// Quick Stats Row — REDESIGNED: asymmetric bento (one wide hero stat +
// two stacked compact stats) instead of three equal columns
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickStatsRow(uiState: DistanceDataUiState, selectedFilter: FilterRange) {
    val peakDay = uiState.chartData.maxByOrNull { it.value }

    val (peakLabel, activeLabel, activeUnit) = when (selectedFilter) {
        FilterRange.WEEK -> Triple("Peak day", "Active days", "days")
        FilterRange.MONTH -> Triple("Peak week", "Active weeks", "weeks")
        FilterRange.YEAR -> Triple("Peak month", "Active months", "months")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wide hero stat — total distance, full height, left-aligned bento block
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = StatCardGreen.copy(alpha = 0.8f))
                .clip(RoundedCornerShape(20.dp))
                .background(StatCardGreen)
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.TrendingUp, null, tint = Color(0xFF2E7D4F), modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        "%,.2f".format(uiState.totalDistance),
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFF1A1A2E), fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                    )
                    Text(
                        stringResource(R.string.total) + " (" + stringResource(R.string.km) + ")",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF5C5C7A))
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
                bgColor = StatCardOrange,
                icon = Icons.Rounded.Bolt,
                iconTint = Color(0xFF9A6B2E),
                label = peakLabel,
                value = peakDay?.displayLabel ?: "–"
            )
            CompactStatCard(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                bgColor = StatCardTeal,
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
            Text(value, style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF5C5C7A), fontSize = 10.sp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chart Card — now takes a header title inline instead of being unlabeled
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ChartCard(headerText: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(CardRadius), ambientColor = HeroCardColor.copy(alpha = 0.18f))
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
                stringResource(R.string.dist_breakdown),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2D2A3D))
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HeroCardColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(headerText, style = MaterialTheme.typography.labelSmall.copy(color = HeroCardColor, fontWeight = FontWeight.SemiBold))
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
                    .background(if (isActive) HeroCardColor else Color(0xFFCCCCDD))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bar Chart — thicker bars
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DistanceBarChart(chartData: List<ChartData>) {
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
                        thickness = 22.dp,
                        shape = remember { Shape.rounded(allPercent = 35) }
                    )
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


// ─────────────────────────────────────────────────────────────────────────────
// Distance Split — REDESIGNED: donut and legend now sit side-by-side
// instead of stacked, more compact and professional
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DistanceSplitCard(splitData: DistanceSplitData) {
    val hasData = splitData.lessThan5k > 0f || splitData.fiveTo10k > 0f || splitData.moreThan10k > 0f

    val PrimaryText = Color(0xFF1A1A2E)
    val MutedText = Color(0xFF8888A8)

    val ColorLessThan5k = Color(0xFF6FCF97)
    val Color5To10k = Color(0xFF6D93BD)
    val ColorMoreThan10k = Color(0xFFE5A15C)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = HeroCardColor.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.distance_split_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryText)
        )
        Text(
            text = stringResource(R.string.by_run_length),
            style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Side-by-side: donut left, legend right
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(
                modifier = Modifier.size(130.dp)
            ) {
                val strokeWidth = 26.dp.toPx()
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                var startAngle = -90f

                if (!hasData) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke
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

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                LegendItem(color = ColorLessThan5k, label = "< 5 km", percentage = splitData.lessThan5k)
                LegendItem(color = Color5To10k, label = "5 – 10 km", percentage = splitData.fiveTo10k)
                LegendItem(color = ColorMoreThan10k, label = "> 10 km", percentage = splitData.moreThan10k)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF5C5C7A), fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1A1A2E), fontWeight = FontWeight.Bold)
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