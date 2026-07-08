package com.example.runtracker.ui.screens

import android.app.Activity
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
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fitnessapp.R

// ─── Palette — matches your Auth/Home/Analytics screens ───
private val TrackingBg = Color(0xFFF7F5FC)
private val TrackingViolet = Color(0xFF8B5CF6)
private val TrackingVioletLight = Color(0xFFB18AFF)
private val TrackingCoral = Color(0xFFFF8A7A)
private val TrackingCoralDeep = Color(0xFFE0665A)
private val TrackingMint = Color(0xFFB8F2D0)
private val TrackingSkyBlue = Color(0xFFAEE1FF)
private val TrackingCard = Color(0xFFFFFFFF)
private val TrackingTextPrimary = Color(0xFF2D2A3D)
private val TrackingTextSecondary = Color(0xFF8A8599)

private val TrackingGradient = Brush.linearGradient(colors = listOf(TrackingViolet, TrackingVioletLight))
private val TrackingStopGradient = Brush.linearGradient(colors = listOf(TrackingCoral, TrackingCoralDeep))

// ─── Entry Point ──────────────────────────────────────────────────────────────

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun TrackingScreen(navController: NavController) {
    val trackingViewModel: TrackingViewModel = hiltViewModel()
    val uiState by trackingViewModel.trackingUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showNoMovementDialog by remember { mutableStateOf(false) }
    if (showNoMovementDialog) {
        NoMovementAlertDialog(onDismiss = { showNoMovementDialog = false })
    }

    val activity = context as? Activity
    val permissionsToRequest = remember {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }.toTypedArray()
    }

    var showPermissionRationale by remember { mutableStateOf(false) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val fineLocationGranted = permissionsMap[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissionsMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            trackingViewModel.startRun()
        } else {
            val shouldShowLocationRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_COARSE_LOCATION)
            } ?: false

            isPermanentlyDenied = !shouldShowLocationRationale
            showPermissionRationale = true
        }
    }

    LaunchedEffect(Unit) {
        trackingViewModel.uiEvent.collect { event ->
            when (event) {
                is TrackingUiEvent.StartRunService -> {
                    trackingViewModel.startIntent()
                }
                is TrackingUiEvent.RequestEnableLocation -> {
                    val currentActivity = context as? Activity
                    event.exception.startResolutionForResult(currentActivity!!, 1001)
                }
                is TrackingUiEvent.ShowLocationError -> {
                    Toast.makeText(context, context.getString(R.string.tracking_gps_lost), Toast.LENGTH_LONG).show()
                }
                is TrackingUiEvent.LocationRestored -> {
                    Toast.makeText(context, context.getString(R.string.tracking_gps_restored), Toast.LENGTH_SHORT).show()
                }
                is TrackingUiEvent.ShowNoMovementDialogue -> {
                    showNoMovementDialog = true
                }
            }
        }
    }

    val isRunning = uiState.startTime.isNotEmpty()

    TrackingScreenContent(
        uiState = uiState,
        isRunning = isRunning,
        onStart = {
            val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                trackingViewModel.startRun()
            } else {
                multiplePermissionsLauncher.launch(permissionsToRequest)
            }
        },
        onStop = { trackingViewModel.stopRun() },
        onBack = { navController.navigateUp() }
    )

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = TrackingCard,
            title = {
                Text(
                    text = if (isPermanentlyDenied) stringResource(R.string.tracking_permissions_blocked_title) else stringResource(R.string.tracking_location_required_title),
                    fontWeight = FontWeight.Bold,
                    color = TrackingTextPrimary
                )
            },
            text = {
                Text(
                    text = if (isPermanentlyDenied) {
                        stringResource(R.string.tracking_permissions_permanently_denied_desc)
                    } else {
                        stringResource(R.string.tracking_location_rationale_desc)
                    },
                    color = TrackingTextSecondary
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TrackingGradient)
                        .clickable {
                            showPermissionRationale = false
                            if (isPermanentlyDenied) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } else {
                                multiplePermissionsLauncher.launch(permissionsToRequest)
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (isPermanentlyDenied) stringResource(R.string.tracking_open_settings) else stringResource(R.string.tracking_try_again),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text(stringResource(R.string.tracking_cancel), color = TrackingTextSecondary)
                }
            }
        )
    }
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
        sheetPeekHeight = 135.dp,
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetContainerColor = TrackingBg,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 16.dp,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 10.dp)
                    .width(40.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TrackingTextSecondary.copy(alpha = 0.3f))
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
            Box(
                modifier = Modifier
                    .padding(top = 48.dp, start = 18.dp)
                    .align(Alignment.TopStart)
                    .size(46.dp)
                    .shadow(8.dp, CircleShape, ambientColor = TrackingViolet.copy(alpha = 0.3f))
                    .clip(CircleShape)
                    .background(TrackingCard)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.tracking_back_desc),
                    tint = TrackingTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
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
            text = uiState.timerValue.ifEmpty { stringResource(R.string.tracking_timer_default) },
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            ),
            color = TrackingTextPrimary
        )
        Text(
            text = stringResource(R.string.tracking_elapsed_time),
            style = MaterialTheme.typography.labelMedium,
            color = TrackingTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // ── Start Time Row (only while running) ───────────────────────────
        if (isRunning && uiState.startTime.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TrackingVioletLight.copy(alpha = 0.18f))
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.tracking_started_at),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TrackingViolet
                )
                Text(
                    text = uiState.startTime,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TrackingTextPrimary
                )
            }
        }

        // ── Stats Row — bento distance + pace ───────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoStatChip(
                value = uiState.currentDistance.ifEmpty { stringResource(R.string.tracking_distance_default) },
                label = stringResource(R.string.tracking_unit_km),
                bgColor = TrackingSkyBlue,
                icon = Icons.Rounded.Map,
                iconTint = Color(0xFF1A5C73),
                modifier = Modifier.weight(1f)
            )
            BentoStatChip(
                value = uiState.currentPace.ifEmpty { stringResource(R.string.tracking_pace_default) },
                label = stringResource(R.string.tracking_unit_pace),
                bgColor = TrackingMint,
                icon = Icons.Rounded.Speed,
                iconTint = Color(0xFF1F6D4A),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Start / Stop Button ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(78.dp)
                .shadow(
                    16.dp,
                    CircleShape,
                    ambientColor = (if (isRunning) TrackingCoral else TrackingViolet).copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(if (isRunning) TrackingStopGradient else TrackingGradient)
                .clickable { if (isRunning) onStop() else onStart() },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(targetState = isRunning, label = "btn_icon") { running ->
                if (running) {
                    Icon(
                        Icons.Rounded.Stop,
                        contentDescription = stringResource(R.string.tracking_stop_run_desc),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.tracking_start_run_desc),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isRunning) stringResource(R.string.tracking_tap_to_stop) else stringResource(R.string.tracking_tap_to_start),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (isRunning) TrackingCoralDeep else TrackingTextSecondary
        )
    }
}

// ─── Bento Stat Chip ──────────────────────────────────────────────────────────

@Composable
private fun BentoStatChip(
    value: String,
    label: String,
    bgColor: Color,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), ambientColor = bgColor.copy(alpha = 0.7f))
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(vertical = 16.dp, horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = TrackingTextPrimary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TrackingTextPrimary.copy(alpha = 0.6f),
            letterSpacing = 0.6.sp
        )
    }
}

// ─── Idle Map Placeholder ─────────────────────────────────────────────────────

@Composable
private fun IdleMapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(colors = listOf(TrackingVioletLight.copy(alpha = 0.35f), TrackingBg))
        ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 300.dp)) {

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(10.dp, CircleShape, ambientColor = TrackingViolet.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(TrackingCard),
                contentAlignment = Alignment.Center
            ) {
                Text("📍", fontSize = 36.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.tracking_waiting_for_gps),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TrackingTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.tracking_map_placeholder_desc),
                style = MaterialTheme.typography.bodySmall,
                color = TrackingTextSecondary
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

    Row(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(50), ambientColor = TrackingCoral.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(50))
            .background(TrackingStopGradient)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = alpha))
        )
        Text(
            text = stringResource(R.string.tracking_live_badge),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.sp
        )
    }
}