package com.example.fitnessapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTrendsScreen(
    navController : NavController,
    // ← Just pass your click handlers here
    onStepsClick: () -> Unit = {},
    onDistanceClick: () -> Unit = {},
    onEnergyClick: () -> Unit = {},
    onPaceClick: () -> Unit = {},
) {
    Scaffold(
        bottomBar = {
            BottomBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            Text(
                text = "Health Trends",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
            )

            // Vertical list of rectangular cards
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TrendListCard(
                    title = "Steps",
                    icon = Icons.Filled.DirectionsWalk,
                    backgroundColor = Color(0xFFF4B3FF),     // Pink
                    contentColor = Color(0xFF7B3F6B),
                    onClick = onStepsClick
                )

                TrendListCard(
                    title = "Distance",
                    icon = Icons.Filled.DirectionsRun,
                    backgroundColor = Color(0xFF3A5F8A),     // Deep blue
                    contentColor = Color.White,
                    onClick = onDistanceClick
                )

                TrendListCard(
                    title = "Energy",
                    icon = Icons.Filled.LocalFireDepartment,
                    backgroundColor = Color(0xFFF8E7D9),     // Peach
                    contentColor = Color(0xFF8B5E3C),
                    onClick = onEnergyClick
                )

                TrendListCard(
                    title = "Pace",
                    icon = Icons.Filled.Timer,
                    backgroundColor = Color(0xFFE0F0E8),     // Soft green
                    contentColor = Color(0xFF3E6B58),
                    onClick = onPaceClick
                )
            }
        }
    }
}

// ==================== RECTANGULAR CARD ====================

@Composable
private fun TrendListCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),                    // ← Rectangular shape
        shape = RoundedCornerShape(20.dp),      // ← Rounded corners
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
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

            Spacer(modifier = Modifier.width(18.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp
                ),
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ==================== BOTTOM NAV (matches your app) ====================


// ==================== PREVIEWS ====================



