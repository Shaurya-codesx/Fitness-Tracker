package com.example.fitnessapp.ui.activity.Stats.Energy

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
import androidx.compose.material.icons.rounded.LocalFireDepartment
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
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.R

private val CardRadius = 24.dp
private val SectionPadding = 20.dp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnergyAnalyticsScreen(navController: NavController) {
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
        .getEnergyDataForPage(selectedFilter, currentOffset)
        .collectAsState(initial = EnergyUiState())

    Box(
        modifier = Modifier.fillMaxSize().background(EnergyBackground)
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
                        .shadow(4.dp, CircleShape, ambientColor = EnergyChipSelectedBg.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = EnergyChipUnselectedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.energy_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = EnergyTextPrimary
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
            if (currentUiState.totalCalories > 0f) {
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
                        .getEnergyDataForPage(selectedFilter, pageOffset)
                        .collectAsState(initial = EnergyUiState())

                    if (uiState.totalCalories > 0f) {
                        EnergyBarChart(chartData = uiState.chartData)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.energy_no_data_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = EnergyTextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.energy_no_data_subtitle),
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color.LightGray)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                PagerDots(currentPage = pagerState.currentPage, initialPage = startPage)
            }

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
private fun HeroSummaryCard(
    headerText: String,
    uiState: EnergyUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(CardRadius), ambientColor = EnergyHeroPrimary.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(CardRadius))
            .background(Brush.verticalGradient(listOf(EnergyHeroPrimary, EnergyHeroAccent)))
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                style = MaterialTheme.typography.labelLarge.copy(color = EnergyOnHeroSubText, fontWeight = FontWeight.Medium)
            )
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = stringResource(R.string.content_desc_previous),
                        tint = EnergyOnHeroText,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.25f)))
                IconButton(onClick = onNext, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.content_desc_next),
                        tint = EnergyOnHeroText,
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
                        text = stringResource(R.string.energy_daily_avg_burn),
                        style = MaterialTheme.typography.labelMedium.copy(color = EnergyOnHeroSubText)
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = uiState.dailyAverage.toInt().toString(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = EnergyOnHeroText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 52.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.energy_kcal_unit),
                            style = MaterialTheme.typography.titleSmall.copy(color = EnergyOnHeroSubText),
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
                        Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = EnergyOnHeroText,
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
private fun QuickStatsRow(uiState: EnergyUiState, selectedFilter: FilterRange) {
    val bestDay = uiState.chartData.maxByOrNull { it.value }

    val peakLabel = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.energy_peak_label_week)
        FilterRange.MONTH -> stringResource(R.string.energy_peak_label_month)
        FilterRange.YEAR -> stringResource(R.string.energy_peak_label_year)
    }
    val activeLabel = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.energy_active_label_week)
        FilterRange.MONTH -> stringResource(R.string.energy_active_label_month)
        FilterRange.YEAR -> stringResource(R.string.energy_active_label_year)
    }
    val activeUnit = when (selectedFilter) {
        FilterRange.WEEK -> stringResource(R.string.energy_active_unit_days)
        FilterRange.MONTH -> stringResource(R.string.energy_active_unit_weeks)
        FilterRange.YEAR -> stringResource(R.string.energy_active_unit_months)
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wide hero stat — total burn
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = EnergyStatCardPrimary.copy(alpha = 0.8f))
                .clip(RoundedCornerShape(20.dp))
                .background(EnergyStatCardPrimary)
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
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = EnergyHeroAccent, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(
                        uiState.totalCalories.toInt().toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(color = EnergyTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                    )
                    Text(
                        stringResource(R.string.energy_total_burn) + " (" + stringResource(R.string.energy_kcal_unit) + ")",
                        style = MaterialTheme.typography.labelSmall.copy(color = EnergyMutedText)
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
                bgColor = EnergyStatCardSecondary,
                icon = Icons.Rounded.Bolt,
                iconTint = Color(0xFF9A6B2E),
                label = peakLabel,
                value = bestDay?.displayLabel ?: "–"
            )
            CompactStatCard(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                bgColor = EnergyStatCardTertiary,
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
            Text(value, style = MaterialTheme.typography.titleSmall.copy(color = EnergyTextPrimary, fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = EnergyMutedText, fontSize = 10.sp))
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
            .shadow(8.dp, RoundedCornerShape(CardRadius), ambientColor = EnergyHeroPrimary.copy(alpha = 0.18f))
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
                stringResource(R.string.energy_chart_card_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EnergyTextPrimary)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EnergyHeroPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(headerText, style = MaterialTheme.typography.labelSmall.copy(color = EnergyHeroPrimary, fontWeight = FontWeight.SemiBold))
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
                    .background(if (isActive) EnergyHeroPrimary else Color(0xFFCCCCDD))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bar Chart — thicker bars, matches Distance screen thickness
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
    val startAxisValueFormatter = CartesianValueFormatter { y, _, _ -> y.toInt().toString() }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        color = EnergyHeroPrimary,
                        thickness = 22.dp, // matches Distance screen
                        shape = remember { Shape.rounded(allPercent = 35) }
                    )
                )
            ),
            startAxis = rememberStartAxis(
                label = rememberAxisLabelComponent(color = EnergyChipUnselectedText),
                valueFormatter = startAxisValueFormatter,
                guideline = rememberAxisGuidelineComponent(color = Color.LightGray.copy(alpha = 0.5f))
            ),
            bottomAxis = rememberBottomAxis(
                label = rememberAxisLabelComponent(color = EnergyChipUnselectedText),
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
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) EnergyChipSelectedBg else EnergyChipUnselectedBg,
                animationSpec = tween(200), label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) EnergyChipSelectedText else EnergyChipUnselectedText,
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
// Intensity Split — side-by-side donut + legend, matches Distance screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EnergySplitCard(splitData: EnergySplitData) {
    val hasData = splitData.lightRecoveryPct > 0f || splitData.solidEffortPct > 0f || splitData.epicBurnPct > 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = EnergyHeroPrimary.copy(alpha = 0.18f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.energy_intensity_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EnergyTextPrimary)
        )
        Text(
            text = stringResource(R.string.energy_intensity_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = EnergyTextSecondary)
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
                    val sweep1 = (splitData.lightRecoveryPct / 100f) * 360f
                    if (sweep1 > 0) {
                        drawArc(color = EnergyDonutLight, startAngle = startAngle, sweepAngle = sweep1, useCenter = false, style = stroke)
                        startAngle += sweep1
                    }
                    val sweep2 = (splitData.solidEffortPct / 100f) * 360f
                    if (sweep2 > 0) {
                        drawArc(color = EnergyDonutSolid, startAngle = startAngle, sweepAngle = sweep2, useCenter = false, style = stroke)
                        startAngle += sweep2
                    }
                    val sweep3 = (splitData.epicBurnPct / 100f) * 360f
                    if (sweep3 > 0) {
                        drawArc(color = EnergyDonutEpic, startAngle = startAngle, sweepAngle = sweep3, useCenter = false, style = stroke)
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                LegendItem(color = EnergyDonutLight, label = stringResource(R.string.energy_legend_light), percentage = splitData.lightRecoveryPct)
                LegendItem(color = EnergyDonutSolid, label = stringResource(R.string.energy_legend_solid), percentage = splitData.solidEffortPct)
                LegendItem(color = EnergyDonutEpic, label = stringResource(R.string.energy_legend_epic), percentage = splitData.epicBurnPct)
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
            style = MaterialTheme.typography.bodyMedium.copy(color = EnergyMutedText, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(color = EnergyTextPrimary, fontWeight = FontWeight.Bold)
        )
    }
}

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