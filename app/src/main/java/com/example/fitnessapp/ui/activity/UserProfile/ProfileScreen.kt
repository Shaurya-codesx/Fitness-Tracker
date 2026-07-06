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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext // ADDED for Toasts
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            title = { Text("Log Out", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E3A)) },
            text = {
                Text(
                    "Are you sure you want to log out? This will clear your local data from this device.",
                    color = Color(0xFF6B6B7A)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logOut {
                            navController.navigate("authScreen") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Yes, Log out", color = Color(0xFFD96C6C), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF4A5C82))
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        bottomBar = { BottomBar(navController) },
        containerColor = Color(0xFFF5F5FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5FA))
                .padding(paddingValues)
                .padding(horizontal = 18.dp)
        ) {
            when (val state = profileState) {
                is ProfileState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF4A5C82)
                    )
                }
                is ProfileState.Error -> {
                    Text(
                        "Error: ${state.message}",
                        color = Color(0xFFD96C6C),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProfileState.Empty, is ProfileState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = if (state is ProfileState.Empty) "Complete Your Profile" else "My Profile",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 32.sp
                            ),
                            color = Color(0xFF2E2E3A),
                            modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // ---- Hero avatar card ----
                                ProfileHeroCard(
                                    name = state.data.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.1f)
                                )

                                // ---- Bento stat row: Weight + Height ----
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    ProfileStatCard(
                                        icon = Icons.Default.MonitorWeight,
                                        label = "Weight",
                                        value = "${state.data.weight} kg",
                                        backgroundColor = Color(0xFFF8E7D9),
                                        contentColor = Color(0xFF8B5E3C),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                    ProfileStatCard(
                                        icon = Icons.Default.Height,
                                        label = "Height",
                                        value = "${state.data.height} cm",
                                        backgroundColor = Color(0xFFDCF0E4),
                                        contentColor = Color(0xFF3E6B58),
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }

                                // ---- Action buttons ----
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.75f),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    ProfileActionCard(
                                        icon = Icons.Rounded.Logout,
                                        label = "Log Out",
                                        backgroundColor = Color(0xFFFBE2E2),
                                        contentColor = Color(0xFFD96C6C),
                                        onClick = { showLogoutDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                    ProfileActionCard(
                                        icon = Icons.Rounded.Edit,
                                        label = "Edit",
                                        backgroundColor = Color(0xFF4A5C82),
                                        contentColor = Color.White,
                                        onClick = { isEditing = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                            }
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

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4A5C82)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.10f),
                modifier = Modifier
                    .size(170.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 35.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color.White,
                        maxLines = 1
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(contentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = contentColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = contentColor.copy(alpha = 0.75f)
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF4A5C82).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF4A5C82),
                    modifier = Modifier.size(28.dp)
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
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5C82)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        label = { Text(label, color = Color(0xFF9A9AAE)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF5F5FA),
            unfocusedContainerColor = Color(0xFFF5F5FA),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color(0xFF2E2E3A),
            unfocusedTextColor = Color(0xFF2E2E3A),
            cursorColor = Color(0xFF4A5C82)
        )
    )
}