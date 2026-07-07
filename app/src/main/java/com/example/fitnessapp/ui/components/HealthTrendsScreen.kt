package com.example.fitnessapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrendsScreen(
    navController: NavController,
    onStepsClick: () -> Unit = { navController.navigate("stepsScreen") { popUpTo("healthTrendsScreen") } },
    onDistanceClick: () -> Unit = { navController.navigate("distanceScreen") { popUpTo("healthTrendsScreen") } },
    onEnergyClick: () -> Unit = { navController.navigate("EnergyScreen") { popUpTo("healthTrendsScreen") } },
    onPaceClick: () -> Unit = { navController.navigate("PaceScreen") { popUpTo("healthTrendsScreen") } },
) {
    Scaffold(
        bottomBar = { BottomBar(navController) },
        containerColor = Color(0xFFF8F9FE) // Crisp off-white background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // ==================== HEADER WITH BACK BUTTON ====================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        // Adding a tiny shadow to the button itself for depth
                        .shadow(elevation = 2.dp, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1A1A2E),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Health Trends",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color(0xFF1A1A2E)
                )
            }

            // ==================== BENTO GRID ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---- Hero card: Steps (Gradient) ----
                BentoHeroCard(
                    title = "Steps",
                    subtitle = "Track your daily movement",
                    icon = Icons.Filled.DirectionsWalk,
                    onClick = onStepsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                )

                // ---- Bottom row: Distance (big, left) + Energy/Pace stacked (right) ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BentoStatCard(
                        title = "Distance",
                        subtitle = "See how far\nyou've gone",
                        icon = Icons.Filled.DirectionsRun,
                        backgroundColor = Color(0xFFD4E4FF), // Vivid Pastel Blue
                        contentColor = Color(0xFF2B5EA7),
                        onClick = onDistanceClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        iconSize = 40.dp,
                        titleSize = 24.sp
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BentoStatCard(
                            title = "Energy",
                            subtitle = null,
                            icon = Icons.Filled.LocalFireDepartment,
                            backgroundColor = Color(0xFFFFE8D6), // Vivid Pastel Orange
                            contentColor = Color(0xFFD96611),
                            onClick = onEnergyClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            iconSize = 30.dp,
                            titleSize = 15.sp,
                            compact = true
                        )

                        BentoStatCard(
                            title = "Pace",
                            subtitle = null,
                            icon = Icons.Filled.Timer,
                            backgroundColor = Color(0xFFD6F5E1), // Vivid Pastel Green
                            contentColor = Color(0xFF227845),
                            onClick = onPaceClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            iconSize = 30.dp,
                            titleSize = 20.sp,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

// ==================== HERO CARD (GRADIENT) ====================

@Composable
private fun BentoHeroCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Poppy Sunset Gradient
    val heroGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B))
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Elevated
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(heroGradient)
        ) {
            // Decorative oversized faded icon in the background
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp, y = 10.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Bubble
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

// ==================== STAT CARD ====================

@Composable
private fun BentoStatCard(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    titleSize: TextUnit = 20.sp,
    compact: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp), // Increased radius
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // Elevated
    ) {
        if (compact) {
            // Horizontal layout for smaller stacked cards
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = titleSize
                    ),
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            // Vertical layout for the larger side card (Distance)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(iconSize)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = titleSize
                        ),
                        color = contentColor
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.End) // Pushed Chevron to the bottom right
                )
            }
        }
    }
}