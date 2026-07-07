package com.example.fitnessapp.ui.activity.UserProfile

import android.widget.Toast // ADDED for Toasts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // ADDED for Toasts
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.ProfileState
import com.example.fitnessapp.ui.UiStates.ProfileUiState
import com.example.fitnessapp.ui.components.BottomBar
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val profileState by viewModel.profileState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            if (message == "Success") {
                isEditing = false
                Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Log Out", fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)) },
            text = {
                Text(
                    "Are you sure you want to log out? This will clear your local data from this device.",
                    color = Color(0xFF6B6B7A)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logOut {
                        navController.navigate("authScreen") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) {
                    Text("Yes, Log out", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF8888A8))
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        bottomBar = { BottomBar(navController) },
        containerColor = Color(0xFFF8F9FE) // Crisp, slightly cool off-white background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            when (val state = profileState) {
                is ProfileState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF9D7CFF)
                    )
                }
                is ProfileState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        color = Color(0xFFE53935),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProfileState.Empty, is ProfileState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // ─── Header ───
                        Text(
                            text = if (state is ProfileState.Empty) "Complete Profile" else "Statistics",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            ),
                            color = Color(0xFF1A1A2E),
                            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                        )

                        if (state is ProfileState.Empty || isEditing) {
                            EditProfileCard(
                                uiState = uiState,
                                onNameChange = viewModel::onNameChange,
                                onWeightChange = viewModel::onWeightChange,
                                onHeightChange = viewModel::onHeightChange,
                                onSave = { viewModel.saveProfile() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        } else if (state is ProfileState.Success) {
                            // ─── Hero Card ───
                            ProfileHeroCard(
                                name = state.data.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.35f) // Takes up 35% of the remaining screen
                            )

                            // ─── Asymmetrical Bento Grid ───
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.5f), // Takes up 50% of the screen
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Left Side: Tall Card (Weight)
                                ProfileStatCard(
                                    icon = Icons.Default.MonitorWeight,
                                    label = "Weight",
                                    value = "${state.data.weight} kg",
                                    backgroundColor = Color(0xFFA5F3CD), // Mint Green
                                    contentColor = Color(0xFF2A7C55),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )

                                // Right Side: Stacked Cards (Height & Edit)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    ProfileStatCard(
                                        icon = Icons.Default.Height,
                                        label = "Height",
                                        value = "${state.data.height} cm",
                                        backgroundColor = Color(0xFFFBE4D4), // Soft Orange
                                        contentColor = Color(0xFFA66C41),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    )

                                    ProfileActionCard(
                                        icon = Icons.Rounded.Edit,
                                        label = "Edit Profile",
                                        backgroundColor = Color(0xFF9D7CFF), // Vibrant Purple
                                        contentColor = Color.White,
                                        onClick = { isEditing = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    )
                                }
                            }

                            // ─── Bottom Action: Log Out ───
                            ProfileActionCard(
                                icon = Icons.Rounded.Logout,
                                label = "Log Out",
                                backgroundColor = Color(0xFFFFE5E5), // Light Pastel Red
                                contentColor = Color(0xFFD32F2F),
                                onClick = { showLogoutDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.15f) // Snug fit at the bottom
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== HERO AVATAR CARD ====================
@Composable
private fun ProfileHeroCard(
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
    }

    // Matching the gradient from the distance card in the screenshots
    val heroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF8C52FF), Color(0xFFFF914D)) // Purple to Orange/Pink
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(heroGradient)
        ) {
            // Decorative background icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 40.dp, y = 20.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Bubble
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Greeting Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Good evening,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==================== STAT CARD (Weight / Height) ====================
@Composable
private fun ProfileStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Soft shadow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    ),
                    color = contentColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ==================== ACTION CARD (Edit / Log Out) ====================
@Composable
private fun ProfileActionCard(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

// ==================== EDIT / EMPTY STATE CARD ====================
@Composable
private fun EditProfileCard(
    uiState: ProfileUiState,
    onNameChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF9D7CFF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF9D7CFF),
                    modifier = Modifier.size(32.dp)
                )
            }

            ProfilePastelTextField("Full Name", uiState.name, onNameChange, KeyboardType.Text)
            ProfilePastelTextField("Weight (kg)", uiState.weight, onWeightChange, KeyboardType.Decimal)
            ProfilePastelTextField("Height (cm)", uiState.height, onHeightChange, KeyboardType.Number)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D7CFF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePastelTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF8888A8), fontWeight = FontWeight.Medium) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8F9FE),
            unfocusedContainerColor = Color(0xFFF8F9FE),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFF1A1A2E),
            unfocusedTextColor = Color(0xFF1A1A2E),
            cursorColor = Color(0xFF9D7CFF)
        )
    )
}