package com.example.fitnessapp.ui.activity.Stats.Steps

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.ui.activity.Stats.FilterRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import kotlinx.datetime.DayOfWeek
import java.time.temporal.TemporalAdjusters

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StepsAnalyticsScreen() {
    val viewModel: StepsViewModel = hiltViewModel()
    // 1. Manage the selected filter state
    var selectedFilter by remember { mutableStateOf(FilterRange.WEEK) }
// (or FilterRange.WEEK depending on what you named your Enum)

    // Start the Pager in the middle of a massive number to allow infinite left/right swiping
    val initialPage = 10000
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 20000 })

    LaunchedEffect(selectedFilter) {
        // Smoothly slide the user back to 'today'
        pagerState.animateScrollToPage(initialPage)
    }

    // Calculate the current offset from the initial page
    val currentOffset = pagerState.currentPage - initialPage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2. Filter Selector (Week / Month / Year)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterRange.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Dynamic Date Range Header Text
        val headerText = remember(selectedFilter, currentOffset) {
            getFormattedHeader(selectedFilter, currentOffset)
        }
        Text(
            text = headerText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Swipeable Chart Container
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) { page ->
            // Calculate specific offset for this individual page in the pager
            val pageOffset = page - initialPage

            // Collect the data from the flow exposed by the ViewModel
            val chartDataState = viewModel.getStepsDataForPage(selectedFilter, pageOffset)
                .collectAsState(initial = emptyList())

            val chartData = chartDataState.value

            if (chartData.isNotEmpty()) {
                // Render the Vico Bar Chart
                StepsBarChart(chartData = chartData)
            } else {
                // Optional: Loading state or Empty state indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun StepsBarChart(chartData: List<ChartData>) {
    // 1. Extract just the numeric values
    val values = chartData.map { it.value.toFloat() }

    // 2. Build the new Vico 2.0 Cartesian Model
    // We wrap this in a remember block so it only rebuilds when the values change
    val model = remember(values) {
        CartesianChartModel(
            ColumnCartesianLayerModel.build {
                series(values)
            }
        )
    }

    // 3. Render the Chart using CartesianChartHost
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                // The new value formatter passes the x-value, which correlates to your list index
                valueFormatter = { x, _, _ ->
                    chartData.getOrNull(x.toInt())?.displayLabel ?: ""
                }
            )
        ),
        model = model,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Helper to generate clean title strings for the top of the screen based on the swipe offset.
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun getFormattedHeader(filter: FilterRange, offset: Int): String {
    val today = LocalDate.now()
    return when (filter) {
        FilterRange.WEEK -> {
            val targetWeek = today.plusWeeks(offset.toLong())

            val startOfWeek = targetWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val endOfWeek = startOfWeek.plusDays(6)

            val monthDayFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
            val yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.getDefault())

            // 3. Output format: "Jun 15 - Jun 21, 2026"
            "${startOfWeek.format(monthDayFormatter)} - ${endOfWeek.format(monthDayFormatter)}, ${endOfWeek.format(yearFormatter)}"
        }
        FilterRange.MONTH -> {
            val targetMonth = today.plusMonths(offset.toLong())
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            targetMonth.format(formatter)
        }
        FilterRange.YEAR -> {
            val targetYear = today.plusYears(offset.toLong())
            targetYear.year.toString()
        }
    }
}