import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.ui.activity.Onboarding.OnboardingUiState
import com.example.fitnessapp.ui.activity.Onboarding.OnboardingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ─── THEME COLORS ────────────────────────────────────────────────────────
private val BackgroundColor = Color(0xFFF9F9FC)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val SlateBlue = Color(0xFF4A6085)
private val LightBlueAccent = Color(0xFFE4EDFA)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF757575)
private val InputBackground = Color(0xFFF0F2F5)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 3 Pages Total
    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            if (message == "Success") {
                onSetupComplete()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            // ─── BOTTOM NAVIGATION & DOTS ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dot Indicators
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { index ->
                        val color = if (pagerState.currentPage == index) SlateBlue else InputBackground
                        val width = if (pagerState.currentPage == index) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button (Hidden on first page)
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
                        ) {
                            Text("Back", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp)) // Placeholder to keep layout balanced
                    }

                    // Next / Finish Button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            } else {
                                viewModel.completeOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                        shape = CircleShape,
                        modifier = Modifier.height(48.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> ProfilePage(uiState, viewModel)
                2 -> GoalsPage(uiState, viewModel)
            }
        }
    }
}

// ─── PAGE 1: WELCOME ─────────────────────────────────────────────────────
@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(LightBlueAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.DirectionsRun, contentDescription = null, tint = SlateBlue, modifier = Modifier.size(50.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Welcome to the Journey", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        Text("Your offline-first, performance-tracking companion.", style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 48.dp))

        FeatureRow(Icons.Rounded.TrackChanges, "Set Custom Goals", "Tailor your steps, distance, and calories.")
        Spacer(modifier = Modifier.height(24.dp))
        FeatureRow(Icons.Rounded.CloudSync, "Offline First", "Run anywhere. We sync to the cloud when you return.")
    }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceWhite), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = SlateBlue)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ─── PAGE 2: PROFILE ─────────────────────────────────────────────────────
@Composable
fun ProfilePage(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("About You", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        Text("Let's personalize your metrics.", color = TextSecondary, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SurfaceWhite).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OnboardingTextField("Full Name", uiState.name, viewModel::onNameChange, KeyboardType.Text)
            OnboardingTextField("Weight (kg)", uiState.weight, viewModel::onWeightChange, KeyboardType.Decimal)
            OnboardingTextField("Height (cm)", uiState.height, viewModel::onHeightChange, KeyboardType.Number)
        }
    }
}

// ─── PAGE 3: GOALS ───────────────────────────────────────────────────────
@Composable
fun GoalsPage(uiState: OnboardingUiState, viewModel: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Daily Targets", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        Text("What are we aiming for today?", color = TextSecondary, modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))

        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(SurfaceWhite).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OnboardingTextField("Daily Steps", uiState.stepGoal, viewModel::onStepGoalChange, KeyboardType.Number)
            OnboardingTextField("Daily Distance (km)", uiState.distanceGoal, viewModel::onDistanceGoalChange, KeyboardType.Decimal)
            OnboardingTextField("Daily Calories (kcal)", uiState.calorieGoal, viewModel::onCalorieGoalChange, KeyboardType.Number)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingTextField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = InputBackground, unfocusedContainerColor = InputBackground,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
        )
    )
}