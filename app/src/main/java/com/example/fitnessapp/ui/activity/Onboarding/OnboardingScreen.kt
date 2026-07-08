package com.example.fitnessapp.ui.activity.Onboarding

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.R
import com.example.fitnessapp.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            if (message == "Success") onSetupComplete()
            else Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BgColor,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgColor)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        val active = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (active) 28.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (active) HeroBlue else TintLavender)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.back), color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                viewModel.completeOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeroBlue),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(52.dp).widthIn(min = 140.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = if (pagerState.currentPage == 2) stringResource(R.string.get_started) else stringResource(R.string.next),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().background(BgColor).padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> ProfilePage(uiState, viewModel)
                2 -> GoalsPage(uiState, viewModel)
            }
        }
    }
}

// ─── PAGE 1: WELCOME (fully weighted, no dead space) ─────────────────────
@Composable
fun WelcomePage() {
    val context = LocalContext.current
    val activity = context as? Activity
    val isTiramisuOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    var permissionGranted by remember {
        mutableStateOf(
            if (isTiramisuOrLater) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var showNotificationRationale by remember { mutableStateOf(false) }
    var isPermanentlyDenied by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) {
            val shouldShow = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
            } ?: false
            isPermanentlyDenied = !shouldShow
            showNotificationRationale = true
        } else {
            showNotificationRationale = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ─── HERO CARD ─── takes the largest share
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .clip(RoundedCornerShape(32.dp))
                .background(HeroBlue)
        ) {
            // decorative background circles to fill the card's own empty space
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.DirectionsRun, contentDescription = null, tint = OnHero, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = OnHero, lineHeight = 32.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(color = OnHero.copy(alpha = 0.75f), lineHeight = 20.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── FEATURE BENTO ROW ───
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureBentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                tint = TintLavender,
                icon = Icons.Rounded.TrackChanges,
                title = stringResource(R.string.custom_goals_title),
                subtitle = stringResource(R.string.custom_goals_subtitle)
            )
            FeatureBentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                tint = TintMint,
                icon = Icons.Rounded.CloudSync,
                title = stringResource(R.string.offline_first_title),
                subtitle = stringResource(R.string.offline_first_subtitle)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── SECOND FEATURE ROW — fills what used to be dead space ───
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureBentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                tint = TintPink,
                icon = Icons.Rounded.Insights,
                title = stringResource(R.string.real_insights_title),
                subtitle = stringResource(R.string.real_insights_subtitle)
            )
            FeatureBentoCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                tint = TintPeach,
                icon = Icons.Rounded.EmojiEvents,
                title = stringResource(R.string.personal_bests_title),
                subtitle = stringResource(R.string.personal_bests_subtitle)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── NOTIFICATION / STATUS STRIP — always present, fills bottom ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (permissionGranted || !isTiramisuOrLater) TintMint else TintPeach)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (permissionGranted || !isTiramisuOrLater) Icons.Rounded.CheckCircle else Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = HeroBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (permissionGranted || !isTiramisuOrLater) stringResource(R.string.all_set_title) else stringResource(R.string.live_run_stats_title),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    if (permissionGranted || !isTiramisuOrLater) stringResource(R.string.notifications_enabled_subtitle)
                    else stringResource(R.string.enable_notifications_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (!permissionGranted && isTiramisuOrLater) {
                TextButton(onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text(stringResource(R.string.enable), color = HeroBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showNotificationRationale) {
        AlertDialog(
            onDismissRequest = { showNotificationRationale = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = if (isPermanentlyDenied) stringResource(R.string.permission_blocked_title) else stringResource(R.string.live_stats_required_title),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = if (isPermanentlyDenied) {
                        stringResource(R.string.permission_permanently_denied_message)
                    } else {
                        stringResource(R.string.permission_rationale_message)
                    },
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationRationale = false
                        if (isPermanentlyDenied) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text(text = if (isPermanentlyDenied) stringResource(R.string.open_settings) else stringResource(R.string.try_again), color = HeroBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationRationale = false }) {
                    Text(stringResource(R.string.not_now), color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun FeatureBentoCard(modifier: Modifier = Modifier, tint: Color, icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(tint).padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HeroBlue, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
        }
    }
}

// ─── PAGE 2: PROFILE (form + live BMI insight fills remaining height) ────
@Composable
fun ProfilePage(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    val weightVal = uiState.weight.toFloatOrNull()
    val heightVal = uiState.height.toFloatOrNull()
    val bmi = if (weightVal != null && heightVal != null && heightVal > 0) {
        val heightM = heightVal / 100f
        weightVal / (heightM * heightM)
    } else null

    val (bmiLabel, bmiTint) = when {
        bmi == null -> stringResource(R.string.enter_weight_height) to TintLavender
        bmi < 18.5 -> stringResource(R.string.bmi_underweight) to TintPeach
        bmi < 25 -> stringResource(R.string.bmi_healthy) to TintMint
        bmi < 30 -> stringResource(R.string.bmi_above_healthy) to TintPeach
        else -> stringResource(R.string.bmi_high) to TintPink
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
                .clip(RoundedCornerShape(32.dp))
                .background(TintLavender),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(HeroBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(R.string.about_you), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                Text(stringResource(R.string.personalize_metrics_subtitle), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OnboardingTextField(stringResource(R.string.full_name), uiState.name, viewModel::onNameChange, KeyboardType.Text, Icons.Rounded.Badge)
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OnboardingTextField(stringResource(R.string.weight_kg), uiState.weight, viewModel::onWeightChange, KeyboardType.Decimal, Icons.Rounded.MonitorWeight)
                }
                Box(modifier = Modifier.weight(1f)) {
                    OnboardingTextField(stringResource(R.string.height_cm), uiState.height, viewModel::onHeightChange, KeyboardType.Number, Icons.Rounded.Height)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live BMI insight card — fills the bottom, updates as user types
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
                .clip(RoundedCornerShape(24.dp))
                .background(bmiTint)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Favorite, contentDescription = null, tint = HeroBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.bmi_title), fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Text(bmiLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (bmi != null) {
                Text(
                    text = String.format("%.1f", bmi),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = HeroBlue)
                )
            }
        }
    }
}

// ─── PAGE 3: GOALS (fills space with a computed summary card) ───────────
@Composable
fun GoalsPage(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    val steps = uiState.stepGoal.toIntOrNull()
    val estKm = steps?.let { (it * 0.0008f) } // rough steps-to-km estimate for the summary card

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(stringResource(R.string.daily_targets_title), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        Text(stringResource(R.string.aiming_for_today_subtitle), color = TextSecondary, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

        GoalBentoCard(
            tint = TintLavender,
            icon = Icons.Rounded.DirectionsWalk,
            label = stringResource(R.string.daily_steps),
            value = uiState.stepGoal,
            onValueChange = viewModel::onStepGoalChange,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().weight(1.2f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GoalBentoCard(
                tint = TintMint,
                icon = Icons.Rounded.Straighten,
                label = stringResource(R.string.daily_distance_km),
                value = uiState.distanceGoal,
                onValueChange = viewModel::onDistanceGoalChange,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                vertical = true
            )
            GoalBentoCard(
                tint = TintPink,
                icon = Icons.Rounded.LocalFireDepartment,
                label = stringResource(R.string.calories),
                value = uiState.calorieGoal,
                onValueChange = viewModel::onCalorieGoalChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                vertical = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary / motivational insight card — fills the remaining bottom space
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .clip(RoundedCornerShape(24.dp))
                .background(TintPeach)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = HeroBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(stringResource(R.string.your_daily_target), fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (steps != null) stringResource(R.string.daily_target_est_subtitle, estKm ?: 0f) else stringResource(R.string.set_step_goal_estimate_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalBentoCard(
    modifier: Modifier = Modifier,
    tint: Color,
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    vertical: Boolean = false
) {
    val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HeroBlue, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                cursorBrush = SolidColor(HeroBlue),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(stringResource(R.string.zero), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary.copy(alpha = 0.4f)))
                    }
                    inner()
                }
            )
        }
    }

    if (vertical) {
        Column(
            modifier = modifier.clip(RoundedCornerShape(24.dp)).background(tint).padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) { content() }
    } else {
        Row(
            modifier = modifier.clip(RoundedCornerShape(24.dp)).background(tint).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) { content() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingTextField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType, icon: ImageVector) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = TintLavender,
            unfocusedContainerColor = BgColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLabelColor = HeroBlue,
            cursorColor = HeroBlue
        )
    )
}
