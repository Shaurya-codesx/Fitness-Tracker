package com.example.fitnessapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        containerColor = Color(0xFFF5F5FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5FA))
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            // Title
            Text(
                text = "Health Trends",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp
                ),
                color = Color(0xFF2E2E3A),
                modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)
            )

            // ==================== BENTO GRID ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ---- Hero card: Steps ----
                BentoHeroCard(
                    title = "Steps",
                    subtitle = "Track your daily movement",
                    icon = Icons.Filled.DirectionsWalk,
                    backgroundColor = Color(0xFF4A5C82),
                    contentColor = Color.White,
                    onClick = onStepsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.3f)
                )

                // ---- Bottom row: Distance (big, left) + Energy/Pace stacked (right) ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    BentoStatCard(
                        title = "Distance",
                        subtitle = "See how far\nyou've gone",
                        icon = Icons.Filled.DirectionsRun,
                        backgroundColor = Color(0xFFFBE7E7),
                        contentColor = Color(0xFFE57575),
                        onClick = onDistanceClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        iconSize = 40.dp,
                        titleSize = 22.sp
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        BentoStatCard(
                            title = "Energy",
                            subtitle = null,
                            icon = Icons.Filled.LocalFireDepartment,
                            backgroundColor = Color(0xFFF8E7D9),
                            contentColor = Color(0xFF8B5E3C),
                            onClick = onEnergyClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            iconSize = 30.dp,
                            titleSize = 18.sp,
                            compact = true
                        )

                        BentoStatCard(
                            title = "Pace",
                            subtitle = null,
                            icon = Icons.Filled.Timer,
                            backgroundColor = Color(0xFFDCF0E4),
                            contentColor = Color(0xFF3E6B58),
                            onClick = onPaceClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            iconSize = 30.dp,
                            titleSize = 18.sp,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

// ==================== HERO CARD ====================

@Composable
private fun BentoHeroCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative oversized faded icon in the background
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(contentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = contentColor.copy(alpha = 0.75f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

// ==================== STAT CARD (used for Distance / Energy / Pace) ====================

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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (compact) {
            // Horizontal layout for smaller stacked cards
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = titleSize
                    ),
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.55f),
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            // Vertical layout for the larger side card (Distance)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(contentColor.copy(alpha = 0.18f)),
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = titleSize
                        ),
                        color = contentColor
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = contentColor.copy(alpha = 0.75f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.55f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

