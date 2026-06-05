package com.example.fitnessapp.ui.activity.UserProfile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fitnessapp.ui.UiStates.ProfileState
import com.example.fitnessapp.ui.components.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {

    val viewModel : ProfileViewModel = hiltViewModel()
    // Observe the database state (Success, Loading, Error)
    val profileState by viewModel.profileState.collectAsState()

    // Observe the current text field values from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Local state to toggle between viewing and editing
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("User Profile") })
        },
        bottomBar = {
            BottomBar(navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = profileState) {
                is ProfileState.Loading -> CircularProgressIndicator()
                is ProfileState.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                is ProfileState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isEditing) {
                            // EDIT MODE: TextFields
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = { viewModel.onNameChange(it) },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.weight,
                                onValueChange = { viewModel.onWeightChange(it) },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = uiState.height,
                                onValueChange = { viewModel.onHeightChange(it) },
                                label = { Text("Height (cm)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    viewModel.saveProfile()
                                    isEditing = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Save Profile")
                            }
                        } else {
                            // DISPLAY MODE: Static Text
                            ProfileInfoRow(label = "Name", value = state.data.name)
                            ProfileInfoRow(label = "Weight", value = "${state.data.weight} kg")
                            ProfileInfoRow(label = "Height", value = "${state.data.height} cm")

                            Spacer(modifier = Modifier.height(16.dp))

                            ExtendedFloatingActionButton(
                                onClick = { isEditing = true },
                                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                text = { Text("Edit Info") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}