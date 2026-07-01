import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnessapp.ui.Auth.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

// ─── THEME COLORS ────────────────────────────────────────────────────────
private val BackgroundColor = Color(0xFFF9F9FC)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val SlateBlue = Color(0xFF4A6085)
private val LightBlueAccent = Color(0xFFE4EDFA)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF757575)
private val InputBackground = Color(0xFFF0F2F5)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,       // <-- NEW: For returning users
    onNavigateToOnboarding: () -> Unit  // <-- NEW: For brand new users
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current // For the Toast

    // ─── THE NEW EVENT LISTENER ───
    // Notice how we completely removed the old `uiState.isSuccess` listener.
    // Everything is now cleanly handled by the ViewModel's Flare Gun (uiEvent).
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                "ReturningUser" -> onNavigateToHome()
                "NewUser" -> onNavigateToOnboarding()
                else -> Toast.makeText(context, event, Toast.LENGTH_LONG).show() // Catches errors and Forgot Password toasts!
            }
        }
    }

    // ... (The rest of your UI, Dialogs, and Form remains exactly the same below this line)

    // ─── FORGOT PASSWORD DIALOG ────────────────────────────────────────
    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = viewModel::toggleResetDialog,
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text("Enter your email address and we will send you a link to reset your password.", color = TextSecondary, modifier = Modifier.padding(bottom = 16.dp))
                    TextField(
                        value = uiState.resetEmailInput,
                        onValueChange = viewModel::onResetEmailChange,
                        placeholder = { Text("Email address", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::sendPasswordResetEmail,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                    shape = CircleShape
                ) {
                    Text("Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::toggleResetDialog) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite
        )
    }
    // ───────────────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // App Icon / Logo Area
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LightBlueAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.DirectionsRun,
                    contentDescription = null,
                    tint = SlateBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Header Text
            Text(
                text = if (uiState.isLoginMode) "Welcome Back" else "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Text(
                text = if (uiState.isLoginMode) "Sign in to sync your runs" else "Start tracking your journey",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceWhite)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Email Field
                TextField(
                    value = uiState.emailInput,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("Email address", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                TextField(
                    value = uiState.passwordInput,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = { Text("Password", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, "Toggle Password Visibility", tint = TextSecondary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // NEW: Forgot Password Text Button
                if (uiState.isLoginMode) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        TextButton(onClick = viewModel::toggleResetDialog) {
                            Text("Forgot Password?", color = SlateBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Error Message
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Action Button
                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateBlue),
                    shape = CircleShape
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (uiState.isLoginMode) "Sign In" else "Sign Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Toggle Mode Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleAuthMode() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (uiState.isLoginMode) "Don't have an account? " else "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Text(
                    text = if (uiState.isLoginMode) "Sign Up" else "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SlateBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}