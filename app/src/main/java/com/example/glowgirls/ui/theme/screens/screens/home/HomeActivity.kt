package com.example.glowgirls.ui.theme.screens.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.glowgirls.data.profile.FirebaseProfileManager
import com.example.glowgirls.models.profile.UserProfile
import com.example.glowgirls.navigation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun HomeScreen(navController: NavController, username: String = "Gorgeous", userId: String = "defaultUserId") {
    // Interactive gradient background with particle system
    val mainGradient = listOf(
        Color(0xFFFFF6FA),  // Ultra light pink
        Color(0xFFFFE4F0),  // Soft pink
        Color(0xFFFBE4FF)   // Soft lavender
    )

    // Shimmer and glow effects colors
    val glowColors = listOf(
        Color(0xFFFF80AB),  // Pink
        Color(0xFF9575CD),  // Purple
        Color(0xFF4FC3F7),  // Blue
        Color(0xFFFFD54F)   // Gold
    )

    // Dynamic button gradients with more vibrant combinations
    val buttonGradients = listOf(
        listOf(Color(0xFFFF80AB), Color(0xFFFF1493)),  // Hot pink
        listOf(Color(0xFF80DEEA), Color(0xFF00BCD4)),  // Bright teal
        listOf(Color(0xFFFFF59D), Color(0xFFFFD700)),  // Gold
        listOf(Color(0xFFAED581), Color(0xFF4CAF50)),  // Vibrant green
        listOf(Color(0xFFE1BEE7), Color(0xFF9C27B0))   // Rich purple
    )

    // Animated floating particles
    val particleCount = 25
    val particles = remember {
        List(particleCount) {
            FloatingParticle(
                startPosition = Offset(
                    x = Random.nextFloat() * 1000f,
                    y = Random.nextFloat() * 2000f
                ),
                color = glowColors[Random.nextInt(glowColors.size)].copy(alpha = Random.nextFloat() * 0.3f + 0.1f),
                size = Random.nextFloat() * 15f + 5f,
                speed = Random.nextFloat() * 1.5f + 0.5f
            )
        }
    }

    // Load user profile
    var userProfile by remember { mutableStateOf(UserProfile()) }
    val firebaseManager = FirebaseProfileManager()

    LaunchedEffect(userId) {
        firebaseManager.getUserProfile(userId) { profile, error ->
            profile?.let { userProfile = it }
        }
    }

    // Animation for breathing effect on the entire screen
    val breathingAnimation = rememberInfiniteTransition()
    val breathingScale by breathingAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Shimmer effect for title
    val shimmerColors = listOf(
        Color(0xFFFFB6C1).copy(alpha = 0.6f),
        Color(0xFFFFC0CB).copy(alpha = 0.9f),
        Color(0xFFFF69B4).copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            )
        )
    )

    // Rotate glow for certain elements
    val rotateTransition = rememberInfiniteTransition()
    val rotationAngle by rotateTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        )
    )

    // Bubble animation for the welcome card
    val coroutineScope = rememberCoroutineScope()
    var bubbleAnimationTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(true) {
            delay(10000)  // Trigger bubble animation every 10 seconds
            bubbleAnimationTrigger = true
            delay(3000)
            bubbleAnimationTrigger = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    Color.White.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .drawBehind {
                            drawGlowingLine(translateAnim.value, shimmerColors)
                        }
                ) {
                    // Animated title with brush
                    val brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF4081),
                            Color(0xFFAA00FF),
                            Color(0xFFFF4081)
                        ),
                        start = Offset(translateAnim.value - 1000f, 0f),
                        end = Offset(translateAnim.value, 0f)
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✨ Glow Circle ✨",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            style = TextStyle(
                                brush = brush,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Animated small gems in the topbar
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawAnimatedGems(rotationAngle)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(mainGradient))
                .drawBehind {
                    // Draw animated particles
                    for (particle in particles) {
                        drawCircle(
                            color = particle.color,
                            radius = particle.size,
                            center = particle.currentPosition(),
                            alpha = 0.7f,
                            blendMode = BlendMode.Plus
                        )
                    }
                }
        ) {
            // Moving particles animation
            LaunchedEffect(Unit) {
                while(true) {
                    delay(16) // approximately 60fps
                    for (particle in particles) {
                        particle.update()
                    }
                }
            }

            // Main content
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile section with 3D card effect
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .glamorousShadow(
                            glowColor = Color(0xFFFF80AB),
                            offsetY = 8.dp,
                            alpha = 0.3f,
                            borderRadius = 24.dp,
                            spread = 8.dp,
                            blurRadius = 16.dp
                        )
                        .waveBorderAnimation(),
                    onClick = { navController.navigate("profile/$userId") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.85f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bubbleEffect(bubbleAnimationTrigger)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Animated profile image with glow
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .pulseEffect()
                                    .drawBehind {
                                        rotate(rotationAngle) {
                                            drawCircle(
                                                brush = Brush.sweepGradient(
                                                    listOf(
                                                        Color(0xFFFF80AB).copy(alpha = 0.7f),
                                                        Color(0xFFFF80AB).copy(alpha = 0.0f),
                                                        Color(0xFFFF80AB).copy(alpha = 0.0f),
                                                        Color(0xFFFF80AB).copy(alpha = 0.7f)
                                                    )
                                                ),
                                                radius = size.width / 1.8f,
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        userProfile.profilePictureUrl.takeIf { it.isNotEmpty() }
                                            ?: "https://via.placeholder.com/150"
                                    ),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 2.dp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFFFF80AB),
                                                    Color(0xFFFF4081)
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                // Animated rainbow text for welcome message
                                Text(
                                    text = "Hey, $username! 👋",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFF4081),
                                                Color(0xFF9C27B0),
                                                Color(0xFF1E88E5)
                                            )
                                        )
                                    )
                                )

                                // Animated typing effect
                                AnimatedTypewriterText(
                                    text = "Ready to glow extraordinarily today?",
                                    fontSize = 14.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }

                // Feature buttons with staggered reveal
                val items = listOf(
                    FeatureItem(
                        title = "My Cycle",
                        description = "Track and visualize your monthly wellness journey",
                        icon = Icons.Outlined.CalendarMonth,
                        gradientColors = buttonGradients[0],
                        route = ROUTE_CYCLE,
                        delay = 100
                    ),
                    FeatureItem(
                        title = "Budget Glow",
                        description = "Financial wellness with personalized insights",
                        icon = Icons.Outlined.ShoppingBag,
                        gradientColors = buttonGradients[1],
                        route = ROUTE_BUDGET_INPUT,
                        delay = 200
                    ),
                    FeatureItem(
                        title = "Vision Glow",
                        description = "Immersive visualization for your goals & dreams",
                        icon = Icons.Outlined.Visibility,
                        gradientColors = buttonGradients[2],
                        route = ROUTE_VISION,
                        delay = 300
                    ),
                    FeatureItem(
                        title = "Chat Support",
                        description = "Real-time advice from your glow community",
                        icon = Icons.Outlined.Chat,
                        gradientColors = buttonGradients[3],
                        route = ROUTE_CHAT,
                        delay = 400
                    ),
                    FeatureItem(
                        title = "My Journal",
                        description = "Capture your glow journey with AI insights",
                        icon = Icons.Outlined.Book,
                        gradientColors = buttonGradients[4],
                        route = "journal_list",
                        delay = 500
                    )
                )

                items.forEach { featureItem ->
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(featureItem.delay.toLong())
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(500)) +
                                expandVertically(animationSpec = tween(500)),
                        exit = fadeOut()
                    ) {
                        FeatureButtonEnhanced(
                            title = featureItem.title,
                            description = featureItem.description,
                            icon = featureItem.icon,
                            gradientColors = featureItem.gradientColors,
                            onClick = { navController.navigate(featureItem.route) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Bottom inspirational quote with glass effect
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "✨ Daily Inspiration ✨",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFFF4081)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "\"You are glowing from within, and today is your day to shine even brighter.\"",
                            fontSize = 14.sp,
                            color = Color(0xFF333333),
                            modifier = Modifier.sparkleEffect()
                        )
                    }
                }
            }

            // Floating action button with pulsating effect
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            bubbleAnimationTrigger = true
                            delay(3000)
                            bubbleAnimationTrigger = false
                        }
                    },
                    containerColor = Color(0xFFFF4081),
                    shape = CircleShape,
                    modifier = Modifier
                        .pulseEffect()
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF80AB).copy(alpha = 0.5f),
                                        Color.Transparent
                                    ),
                                    radius = size.width * breathingScale
                                ),
                                radius = size.width
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Spa,
                        contentDescription = "Refresh Glow",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureButtonEnhanced(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Animation for 3D rotation effect
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(expanded) {
        rotation.animateTo(
            targetValue = if (expanded) 2f else 0f,
            animationSpec = tween(300, easing = EaseOutQuad)
        )
    }

    // Shimmer animation
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.5f),
            Color.Transparent
        ),
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationX = rotation.value
                cameraDistance = 12f * density
            }
            .then(
                if (expanded) Modifier
                    .glamorousShadow(
                        glowColor = gradientColors[1],
                        offsetY = 12.dp,
                        alpha = 0.5f,
                        borderRadius = 20.dp,
                        spread = 2.dp,
                        blurRadius = 16.dp
                    )
                else Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp)
                    )
            )
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(20.dp),
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.85f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon with gradient background and pulse effect
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .pulseEffect()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(gradientColors)
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.8f),
                                        gradientColors[0].copy(alpha = 0.3f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Title with gradient text
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(
                                brush = Brush.linearGradient(gradientColors)
                            )
                        )

                        // Description
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Feature highlights with icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FeatureHighlight(
                            icon = Icons.Outlined.AutoAwesome,
                            text = "Personalized",
                            color = gradientColors[0]
                        )

                        FeatureHighlight(
                            icon = Icons.Outlined.TrendingUp,
                            text = "Insights",
                            color = gradientColors[1]
                        )

                        FeatureHighlight(
                            icon = Icons.Outlined.Favorite,
                            text = "Wellness",
                            color = gradientColors[0]
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Enhanced button with animation
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shimmerEffect(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(gradientColors)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Explore $title",
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureHighlight(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.2f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .blur(radius = 0.5.dp)
    ) {
        content()
    }
}

@Composable
fun AnimatedTypewriterText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color
) {
    var visibleCharCount by remember { mutableStateOf(0) }

    LaunchedEffect(text) {
        visibleCharCount = 0
        for (i in text.indices) {
            delay(50)
            visibleCharCount = i + 1
        }
    }

    Text(
        text = text.take(visibleCharCount),
        fontSize = fontSize,
        color = color
    )
}

// Extension function for shimmer effect
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.5f),
            Color.Transparent
        ),
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )

    background(brush = brush, shape = RoundedCornerShape(16.dp))
}

// Extension function for pulse animation
fun Modifier.pulseEffect(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// Extension function for sparkling text
fun Modifier.sparkleEffect(): Modifier = composed {
    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition()
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    drawBehind {
        val sparkleSizePx = with(density) { 4.dp.toPx() }

        for (i in 0 until 20) {
            val x = Random.nextFloat() * size.width
            val y = Random.nextFloat() * size.height

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = sparkleAlpha * 0.7f),
                radius = sparkleSizePx,
                center = Offset(x, y),
                blendMode = BlendMode.SrcOver
            )
        }
    }
}

// Extension function for glamorous shadow
@RequiresApi(Build.VERSION_CODES.O)
fun Modifier.glamorousShadow(
    glowColor: Color,
    offsetY: androidx.compose.ui.unit.Dp,
    alpha: Float = 0.5f,
    borderRadius: androidx.compose.ui.unit.Dp = 0.dp,
    spread: androidx.compose.ui.unit.Dp = 0.dp,
    blurRadius: androidx.compose.ui.unit.Dp = 0.dp
) = this.drawBehind {
    val transparentColor = glowColor.copy(alpha = 0f).value.toLong().toColorInt()
    val shadowColor = glowColor.copy(alpha = alpha).value.toLong().toColorInt()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor

        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            0f,
            offsetY.toPx(),
            shadowColor
        )

        val spreadPixel = spread.toPx()
        val radiusPixel = borderRadius.toPx()

        it.drawRoundRect(
            left = 0f + spreadPixel,
            top = 0f + spreadPixel,
            right = this.size.width - spreadPixel,
            bottom = this.size.height - spreadPixel,
            radiusX = radiusPixel,
            radiusY = radiusPixel,
            paint = paint
        )
    }
}

// Extension function for wave border animation
fun Modifier.waveBorderAnimation(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    drawBehind {
        val strokeWidth = 2.dp.toPx()
        val cornerRadius = 24.dp.toPx()

        // Draw wave border
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF80AB),
                    Color(0xFF9C27B0),
                    Color(0xFF1E88E5),
                    Color(0xFFFF80AB)
                )
            ),
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(15f, 10f),
                    phase = phase
                )
            ),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
    }
}

// Extension function for bubble animation
fun Modifier.bubbleEffect(isActive: Boolean): Modifier = composed {
    if (!isActive) return@composed this

    val density = LocalDensity.current
    val bubbles = remember {
        List(15) {
            Bubble(
                x = Random.nextFloat() * 100f,
                y = Random.nextFloat() * 100f,
                radius = with(density) { (Random.nextFloat() * 8f + 2f).dp.toPx() },
                alpha = Random.nextFloat() * 0.5f + 0.1f,
                speed = Random.nextFloat() * 0.6f + 0.2f
            )
        }
    }

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            progress = 0f
            val duration = 3000 // 3 seconds
            val steps = 100
            val stepDuration = duration / steps

            for (i in 0..steps) {
                progress = i.toFloat() / steps
                delay(stepDuration.toLong())
            }
        }
    }

    drawBehind {
        for (bubble in bubbles) {
            val centerX = bubble.x * size.width / 100f
            val startY = size.height + bubble.radius
            val endY = -bubble.radius

            val currentY = startY + (endY - startY) * progress * bubble.speed

            drawCircle(
                color = Color(0xFFFF80AB).copy(alpha = bubble.alpha),
                radius = bubble.radius,
                center = Offset(centerX, currentY),
                blendMode = BlendMode.Plus
            )
        }
    }
}

// Draw animated gems in topbar
fun DrawScope.drawAnimatedGems(rotationAngle: Float) {
    val gemCount = 5
    val gemColors = listOf(
        Color(0xFFFF80AB),  // Pink
        Color(0xFF9575CD),  // Purple
        Color(0xFF4FC3F7),  // Blue
        Color(0xFFFFD54F)   // Gold
    )

    for (i in 0 until gemCount) {
        val angle = (i.toFloat() / gemCount) * 2 * PI.toFloat() + rotationAngle
        val radius = size.width * 0.45f
        val x = size.width / 2 + radius * cos(angle)
        val y = size.height / 2 + radius * sin(angle) * 0.2f // Flatter ellipse

        val gemSize = size.width * 0.03f
        val gemColor = gemColors[i % gemColors.size]

        // Draw gem glow
        drawCircle(
            color = gemColor.copy(alpha = 0.3f),
            radius = gemSize * 1.5f,
            center = Offset(x, y)
        )

        // Draw gem
        drawCircle(
            color = gemColor,
            radius = gemSize,
            center = Offset(x, y)
        )
    }
}

// Draw shimmering line in topbar
fun DrawScope.drawGlowingLine(translateAnim: Float, colors: List<Color>) {
    val strokeWidth = size.height * 0.02f
    val yPosition = size.height - strokeWidth / 2

    // Draw glowing line
    drawLine(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(translateAnim - 800f, yPosition),
            end = Offset(translateAnim, yPosition)
        ),
        start = Offset(0f, yPosition),
        end = Offset(size.width, yPosition),
        strokeWidth = strokeWidth
    )
}

// Data class for feature items
data class FeatureItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val gradientColors: List<Color>,
    val route: String,
    val delay: Int
)

// Data class for animated particles
class FloatingParticle(
    private val startPosition: Offset,
    val color: Color,
    val size: Float,
    val speed: Float
) {
    private var time = 0f
    private val amplitude = Random.nextFloat() * 50f + 10f
    private val frequency = Random.nextFloat() * 0.02f + 0.01f

    fun currentPosition(): Offset {
        val x = startPosition.x + sin(time * frequency) * amplitude
        val y = (startPosition.y - time * speed) % 2000f

        return Offset(x, y)
    }

    fun update() {
        time += 1f
    }
}

// Data class for bubble effect
data class Bubble(
    val x: Float,
    var y: Float,
    val radius: Float,
    val alpha: Float,
    val speed: Float
)