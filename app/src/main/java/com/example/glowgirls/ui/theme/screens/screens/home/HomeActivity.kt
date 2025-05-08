package com.example.glowgirls.ui.theme.screens.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glowgirls.R
import com.example.glowgirls.navigation.ROUTE_BUDGET_INPUT
import com.example.glowgirls.navigation.ROUTE_CHAT
import com.example.glowgirls.navigation.ROUTE_CYCLE
import com.example.glowgirls.navigation.ROUTE_VISION

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, username: String = "Gorgeous") {
    // Create feminine color scheme
    val gradientColors = listOf(
        Color(0xFFFFC0CB), // Light Pink
        Color(0xFFFFB6C1), // Pink
        Color(0xFFFFA6C9)  // Rose Pink
    )

    val buttonGradients = listOf(
        listOf(Color(0xFFFF80AB), Color(0xFFFF4081)), // Pink shades
        listOf(Color(0xFF80DEEA), Color(0xFF26C6DA)), // Teal shades
        listOf(Color(0xFFFFD54F), Color(0xFFFFC107)), // Yellow shades
        listOf(Color(0xFFAED581), Color(0xFF8BC34A)), // Green shades
        listOf(Color(0xFFCE93D8), Color(0xFF9C27B0))  // Purple shades
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "✨ Glow Circle ✨",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4081)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF9FA)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(gradientColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile image placeholder
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB6C1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Face,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Hey, $username! 👋",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "Ready to glow today?",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }

                // Feature buttons
                FeatureButton(
                    title = "My Cycle",
                    description = "Track and understand your monthly patterns",
                    icon = Icons.Outlined.CalendarMonth,
                    gradientColors = buttonGradients[0],
                    onClick = { navController.navigate(ROUTE_CYCLE) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    title = "Budget Glow",
                    description = "Make your budget Glow",
                    icon = Icons.Outlined.ShoppingBag,
                    gradientColors = buttonGradients[1],
                    onClick = { navController.navigate(ROUTE_BUDGET_INPUT) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    title = "Vision Glow",
                    description = "Visualize your  goals",
                    icon = Icons.Outlined.Visibility,
                    gradientColors = buttonGradients[2],
                    onClick = { navController.navigate(ROUTE_VISION) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    title = "Chat Support",
                    description = "Get advice from other glow girls",
                    icon = Icons.Outlined.Chat,
                    gradientColors = buttonGradients[3],
                    onClick = { navController.navigate(ROUTE_CHAT) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeatureButton(
                    title = "My Journal",
                    description = "Document your glow journey",
                    icon = Icons.Outlined.Book,
                    gradientColors = buttonGradients[4],
                    onClick = { navController.navigate("journal_list") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(20.dp),
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon circle with gradient background
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(gradientColors)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradientColors[1]
                        )
                    ) {
                        Text(
                            "Go to $title",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}