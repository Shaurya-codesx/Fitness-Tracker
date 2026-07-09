package com.example.fitnessapp.ui.activity.RunHistory.RunDetails

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnessapp.R
// ─── Palette — matches your Auth/Home/Analytics/Tracking screens ───
private val DetailBg = Color(0xFFF7F5FC)
private val DetailViolet = Color(0xFF8B5CF6)
private val DetailVioletLight = Color(0xFFB18AFF)
private val DetailCoral = Color(0xFFFF8A7A)
private val DetailMint = Color(0xFFB8F2D0)
private val DetailSkyBlue = Color(0xFFAEE1FF)
private val DetailPeach = Color(0xFFF3E3D3)
private val DetailCard = Color(0xFFFFFFFF)
private val DetailTextPrimary = Color(0xFF2D2A3D)
private val DetailTextSecondary = Color(0xFF8A8599)

private val DetailHeroGradient = Brush.linearGradient(colors = listOf(DetailViolet, DetailVioletLight))

// ─── Entry Point ──────────────────────────────────────────────────────────────

@Composable
fun RunDetailsScreen(navController: NavController) {
    val runDetailsViewModel: RunDetailsViewModel = hiltViewModel()
    val uiState by runDetailsViewModel.runDetailsState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DetailBg,
        topBar = {
            RunDetailTopBar(onBack = { navController.navigateUp() })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DetailBg)
                .padding(innerPadding)
        ) {
            when (uiState) {
                is RunDetailsState.Loading -> RunDetailLoadingState()
                is RunDetailsState.Error -> RunDetailErrorState(onRetry = {})
                is RunDetailsState.Success -> {
                    val data = (uiState as RunDetailsState.Success).data
                    RunDetailSuccessState(data = data)
                }
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RunDetailTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.run_summary),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = DetailTextPrimary
            )
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(40.dp)
                    .shadow(4.dp, CircleShape, ambientColor = DetailViolet.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(DetailCard)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = DetailTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DetailBg
        )
    )
}

// ─── Success State ────────────────────────────────────────────────────────────

@Composable
private fun RunDetailSuccessState(data: RunDetailsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // ── Route Map ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = DetailViolet.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(28.dp))
        ) {
            miniMapView(routeList = data.routeList)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                        )
                    )
            )

            // Date badge overlaid on map, bottom-left
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = DetailViolet
                )
                Text(
                    text = data.date,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = DetailTextPrimary
                )
            }
        }

        // ── Hero Card — Distance + Duration ────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(28.dp), ambientColor = DetailViolet.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(28.dp))
                .background(DetailHeroGradient)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = data.distance,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 36.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.km),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.total_distance),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = data.duration,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // ── Key Metrics Grid: Pace, Steps, Calories ───────────────────────
        Text(
            text = stringResource(R.string.performance),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DetailTextPrimary,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = painterResource(R.drawable.pace_image),
                label = stringResource(R.string.avg_pace),
                value = data.avgPace,
                unit = stringResource(R.string.min_km),
                bgColor = DetailSkyBlue,
                contentColor = Color(0xFF1A5C73),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = painterResource(R.drawable.steps_image),
                label = stringResource(R.string.steps),
                value = data.stepsTaken,
                unit = stringResource(R.string.steps_unit),
                bgColor = DetailMint,
                contentColor = Color(0xFF1F6D4A),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                icon = painterResource(R.drawable.calories_image),
                label = stringResource(R.string.calories),
                value = data.caloriesBurned,
                unit = "kcal",
                bgColor = DetailPeach,
                contentColor = Color(0xFF9A5B23),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Time Details Card ─────────────────────────────────────────────
        Text(
            text = stringResource(R.string.timeline),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DetailTextPrimary,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = DetailViolet.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(24.dp))
                .background(DetailCard)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Started
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DetailVioletLight.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = DetailViolet,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.started),
                        style = MaterialTheme.typography.labelMedium,
                        color = DetailTextSecondary
                    )
                    Text(
                        text = data.startTime,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DetailTextPrimary
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(start = 62.dp),
                color = DetailTextSecondary.copy(alpha = 0.15f)
            )

            // Finished
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DetailCoral.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Flag,
                        contentDescription = null,
                        tint = DetailCoral,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.finished),
                        style = MaterialTheme.typography.labelMedium,
                        color = DetailTextSecondary
                    )
                    Text(
                        text = data.endTime,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DetailTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Metric Card Component ──────────────────────────────────────────────────

@Composable
private fun MetricCard(
    icon: Painter,
    label: String,
    value: String,
    unit: String,
    bgColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = bgColor.copy(alpha = 0.8f))
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color.White.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = contentColor
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.75f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor.copy(alpha = 0.9f)
        )
    }
}

// ─── Loading State ────────────────────────────────────────────────────────────

@Composable
private fun RunDetailLoadingState() {
    val shimmerColors = listOf(
        DetailVioletLight.copy(alpha = 0.15f),
        DetailVioletLight.copy(alpha = 0.35f),
        DetailVioletLight.copy(alpha = 0.15f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerBrush)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(shimmerBrush)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(shimmerBrush)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(shimmerBrush)
        )
    }
}

// ─── Error State ──────────────────────────────────────────────────────────────

@Composable
private fun RunDetailErrorState(
    message: String = stringResource(R.string.error_msg),
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .shadow(8.dp, CircleShape, ambientColor = DetailCoral.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(DetailCoral.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = DetailCoral,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.couldnt_load_msg),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = DetailTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message.ifBlank { stringResource(R.string.not_load_msg) },
            style = MaterialTheme.typography.bodyMedium,
            color = DetailTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(50), ambientColor = DetailViolet.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(50))
                .background(DetailHeroGradient)
                .clickable { onRetry() }
                .padding(horizontal = 26.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.try_again),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}