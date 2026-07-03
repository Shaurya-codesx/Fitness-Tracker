package com.example.runtracker.ui.screens

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.TrackingUiState
import com.example.fitnessapp.ui.activity.Tracking.NoMovementAlertDialog
import com.example.fitnessapp.ui.activity.Tracking.OsmMapview
import com.example.fitnessapp.ui.activity.Tracking.TrackingUiEvent
import com.example.fitnessapp.ui.activity.Tracking.TrackingViewModel

// ─── Entry Point ──────────────────────────────────────────────────────────────

@Composable
fun TrackingScreen(navController: NavController) {
    val trackingViewModel: TrackingViewModel = hiltViewModel()
    val uiState by trackingViewModel.trackingUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showNoMovementDialog by remember { mutableStateOf(false) }
    if (showNoMovementDialog) {
        NoMovementAlertDialog(onDismiss = { showNoMovementDialog = false })
    }



    LaunchedEffect(Unit) {
        trackingViewModel.uiEvent.collect { event ->
            when (event) {
                is TrackingUiEvent.StartRunService -> {
                    trackingViewModel.startIntent()
                }
                is TrackingUiEvent.RequestEnableLocation -> {
                    Log.d("hello", "Location Error caught")
                    val activity = context as? Activity
                    event.exception.startResolutionForResult(activity!!, 1001)
                }
                is TrackingUiEvent.ShowLocationError -> {
                    Log.d("lokation", "Location error reaching UI")
                    // 1. We removed trackingViewModel.stopRun() here!
                    // 2. Added .show() to actually display the warning
                    Toast.makeText(
                        context,
                        "GPS Signal Lost. Run Paused.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                // 3. NEW: Listen for the signal coming back!
                is TrackingUiEvent.LocationRestored -> {
                    Toast.makeText(
                        context,
                        "GPS Restored. Resuming Run!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is TrackingUiEvent.ShowNoMovementDialogue -> {
                    Log.d("hello", "No movement caught")
                    showNoMovementDialog = true
                }
            }
        }
    }


    val isRunning = uiState.startTime.isNotEmpty()

    TrackingScreenContent(
        uiState = uiState,
        isRunning = isRunning,
        onStart = { trackingViewModel.startRun() },
        onStop = {
            trackingViewModel.stopRun()
                 },
        onBack = { navController.navigateUp() }
    )



}



// ─── Screen Content ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackingScreenContent(
    uiState: TrackingUiState,
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit
) {
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 125.dp,        // how much sheet shows when collapsed
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetTonalElevation = 2.dp,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        },
        sheetContent = {
            SheetContent(
                uiState = uiState,
                isRunning = isRunning,
                onStart = onStart,
                onStop = onStop
            )
        },
        containerColor = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Map ────────────────────────────────────────────────────────
            if (isRunning) {
                OsmMapview(
                    modifier = Modifier.fillMaxSize(),
                    Route = uiState.route
                )
            } else {
                IdleMapPlaceholder(modifier = Modifier.fillMaxSize())
            }

            // ── Back button ────────────────────────────────────────────────
            FilledTonalIconButton(
                onClick = onBack,
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .padding(top = 48.dp, start = 18.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }

            // ── LIVE badge ─────────────────────────────────────────────────
            if (isRunning) {
                LiveBadge(
                    modifier = Modifier
                        .padding(top = 54.dp, end = 18.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

// ─── Sheet Content ────────────────────────────────────────────────────────────

@Composable
private fun SheetContent(
    uiState: TrackingUiState,
    isRunning: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Elapsed Timer ──────────────────────────────────────────────────
        Text(
            text = if (uiState.timerValue.isNotEmpty()) uiState.timerValue else "00:00:00",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Elapsed time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 1.dp, bottom = 16.dp)
        )

        // ── Start Time Row (only while running) ───────────────────────────
        if (isRunning && uiState.startTime.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                    //
                ) {
                    Text(
                        text = "Started at",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.startTime,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // ── Stats Row — Distance + Pace ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatChip(
                value = if (uiState.currentDistance.isNotEmpty()) uiState.currentDistance else "0.0",
                label = "km",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                modifier = Modifier.weight(1f)
            )
            StatChip(
                value = if (uiState.currentPace.isNotEmpty()) uiState.currentPace else "—",
                label = "min / km",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                valueColor = MaterialTheme.colorScheme.onTertiaryContainer,
                labelColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.65f),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Start / Stop Button ────────────────────────────────────────────
        val btnColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            animationSpec = tween(300),
            label = "btn_color"
        )
        val btnContentColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.onError
            else MaterialTheme.colorScheme.onPrimary,
            animationSpec = tween(300),
            label = "btn_content_color"
        )

        FloatingActionButton(
            onClick = { if (isRunning) onStop() else onStart() },
            shape = CircleShape,
            containerColor = btnColor,
            contentColor = btnContentColor,
            modifier = Modifier.size(72.dp)
        ) {
            AnimatedContent(targetState = isRunning, label = "btn_icon") { running ->
                if (running) {
                    Icon(
                        Icons.Rounded.Stop,
                        contentDescription = "Stop run",
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = "Start run",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isRunning) "Tap to stop" else "Tap to start",
            style = MaterialTheme.typography.labelMedium,
            color = if (isRunning) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Idle Map Placeholder ─────────────────────────────────────────────────────

@Composable
private fun IdleMapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📍", fontSize = 40.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Waiting for GPS",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Map will appear when run starts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f)
            )
        }
    }
}

// ─── LIVE Badge ───────────────────────────────────────────────────────────────

@Composable
private fun LiveBadge(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val alpha by pulse.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onError.copy(alpha = alpha))
            )
            Text(
                text = "LIVE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onError,
                letterSpacing = 1.sp
            )
        }
    }
}

// ─── Stat Chip ────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    value: String,
    label: String,
    containerColor: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = valueColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                letterSpacing = 0.6.sp
            )
        }
    }
}