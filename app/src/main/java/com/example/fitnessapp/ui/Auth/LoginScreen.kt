import android.R
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.example.fitnessapp.ui.Auth.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.R.drawable


private val AuthGradient = Brush.linearGradient(
    colors = listOf(AuthViolet, Color(0xFFB18AFF))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                "ReturningUser" -> onNavigateToHome()
                "NewUser" -> onNavigateToOnboarding()
                else -> Toast.makeText(context, event, Toast.LENGTH_LONG).show()
            }
        }
    }

    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = viewModel::toggleResetDialog,
            title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = AuthTextPrimary) },
            text = {
                Column {
                    Text(
                        "Enter your email address and we will send you a link to reset your password.",
                        color = AuthTextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TextField(
                        value = uiState.resetEmailInput,
                        onValueChange = viewModel::onResetEmailChange,
                        placeholder = { Text("Email address", color = AuthTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AuthInputBg,
                            unfocusedContainerColor = AuthInputBg,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::sendPasswordResetEmail,
                    colors = ButtonDefaults.buttonColors(containerColor = AuthViolet),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Send Link", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::toggleResetDialog) {
                    Text("Cancel", color = AuthTextSecondary)
                }
            },
            containerColor = AuthCard,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
    ) {
        // ─── Decorative floating blobs (background flavor) ───
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(AuthSunshine.copy(alpha = 0.5f))
                .align(Alignment.TopStart)
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 40.dp, y = 60.dp)
                .clip(CircleShape)
                .background(AuthMint.copy(alpha = 0.6f))
                .align(Alignment.TopEnd)
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-30).dp, y = 30.dp)
                .clip(CircleShape)
                .background(AuthSkyBlue.copy(alpha = 0.5f))
                .align(Alignment.BottomStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // ─── GIF Hero — layered card with gradient ring ───
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(AuthGradient),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .background(AuthCard),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = drawable.login_page_image,
                        contentDescription = "Running animation",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(28.dp))
                    )
                }
                // small floating badge accent
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(AuthCoral),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (uiState.isLoginMode) "Welcome Back" else "Let's Get Moving",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AuthTextPrimary
                )
            )
            Text(
                text = if (uiState.isLoginMode) "Sign in to sync your runs" else "Create your account to start tracking",
                style = MaterialTheme.typography.bodyMedium.copy(color = AuthTextSecondary),
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            // ─── Segmented pill toggle (replaces plain text toggle) ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(AuthInputBg)
                    .padding(4.dp)
            ) {
                SegmentToggleItem(
                    text = "Sign In",
                    selected = uiState.isLoginMode,
                    modifier = Modifier.weight(1f)
                ) { if (!uiState.isLoginMode) viewModel.toggleAuthMode() }

                SegmentToggleItem(
                    text = "Sign Up",
                    selected = !uiState.isLoginMode,
                    modifier = Modifier.weight(1f)
                ) { if (uiState.isLoginMode) viewModel.toggleAuthMode() }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Form Card ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(AuthCard)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = uiState.emailInput,
                    onValueChange = viewModel::onEmailChange,
                    placeholder = { Text("Email address", color = AuthTextSecondary) },
                    leadingIcon = { Icon(Icons.Rounded.Email, null, tint = AuthViolet) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AuthInputBg,
                        unfocusedContainerColor = AuthInputBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                TextField(
                    value = uiState.passwordInput,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = { Text("Password", color = AuthTextSecondary) },
                    leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = AuthViolet) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(image, "Toggle Password Visibility", tint = AuthTextSecondary)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AuthInputBg,
                        unfocusedContainerColor = AuthInputBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                if (uiState.isLoginMode) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = viewModel::toggleResetDialog) {
                            Text("Forgot Password?", color = AuthViolet, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Gradient Action Button ───
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(CircleShape)
                        .background(AuthGradient)
                        .clickable(enabled = !uiState.isLoading) { viewModel.submit() },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
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

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SegmentToggleItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) AuthCard else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) AuthViolet else AuthTextSecondary,
            fontSize = 14.sp
        )
    }
}