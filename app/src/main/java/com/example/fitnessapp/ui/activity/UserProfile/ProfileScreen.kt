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
import com.example.fitnessapp.ui.components.BottomBar
import kotlinx.coroutines.flow.collectLatest

private val BgColor = Color(0xFFF9F9FC)
private val CardBg = Color.White
private val SlateBlue = Color(0xFF4A6085)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF757575)
private val InputBackground = Color(0xFFF0F2F5)
private val DangerRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val profileState by viewModel.profileState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current // Grab the context for the Toast
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ─── THE EVENT LISTENER ───────────────────────────────────────────────
    // This watches the ViewModel's SharedFlow safely.
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            if (message == "Success") {
                isEditing = false // Only close edit mode if it actually saved
                Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show() // Show the error
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("Are you sure you want to log out? This will clear your local data from this device.", color = TextSecondary) },
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
                    Text("Yes, Log out", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = SlateBlue)
                }
            },
            containerColor = CardBg
        )
    }

    Scaffold(
        bottomBar = { BottomBar(navController) },
        containerColor = BgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = profileState) {
                is ProfileState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = SlateBlue)
                is ProfileState.Error -> Text("Error: ${state.message}", color = DangerRed)
                is ProfileState.Empty, is ProfileState.Success -> {

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (state is ProfileState.Empty) "Complete Your Profile" else "My Profile",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
                        )

                        if (state is ProfileState.Empty || isEditing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(CardBg)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ProfileTextField("Full Name", uiState.name, viewModel::onNameChange, KeyboardType.Text)
                                ProfileTextField("Weight (kg)", uiState.weight, viewModel::onWeightChange, KeyboardType.Decimal)
                                ProfileTextField("Height (cm)", uiState.height, viewModel::onHeightChange, KeyboardType.Number)

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    // ─── UPDATED ONCLICK ───
                                    // Notice we ONLY call saveProfile() here now. The LaunchedEffect handles the rest!
                                    onClick = { viewModel.saveProfile() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Rounded.Save, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (state is ProfileState.Success) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(CardBg)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                ProfileDisplayRow(Icons.Default.Person, "Name", state.data.name)
                                HorizontalDivider(color = InputBackground)
                                ProfileDisplayRow(Icons.Default.MonitorWeight, "Weight", "${state.data.weight} kg")
                                HorizontalDivider(color = InputBackground)
                                ProfileDisplayRow(Icons.Default.Height, "Height", "${state.data.height} cm")
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedButton(
                                    onClick = { showLogoutDialog = true },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = CircleShape,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                                ) {
                                    Icon(Icons.Rounded.Logout, contentDescription = "Log Out")
                                    Spacer(Modifier.width(8.dp))
                                    Text("Log Out", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { isEditing = true },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue)
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDisplayRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(InputBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SlateBlue, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = InputBackground,
            unfocusedContainerColor = InputBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}