import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowgirls.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun SplashScreen(onNavigateToNext: () -> Unit) {
    // Splash screen duration
    val splashScreenDuration = 3500L

    // Animation states
    var isAnimationFinished by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }

    // Logo animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(1000, easing = EaseOutBack)
    )

    // Text animations
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 500)
    )

    // Shimmer effect animation
    val shimmerAnimation = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by shimmerAnimation.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer translate"
    )

    // Particles animation
    val particleAnimation = rememberInfiniteTransition(label = "particles")
    val particleScale by particleAnimation.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle scale"
    )

    // Rotating glow animation
    val rotationAnimation = rememberInfiniteTransition(label = "rotation")
    val rotation by rotationAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "glow rotation"
    )

    // Loading indicator animation
    val loadingAnimation = rememberInfiniteTransition(label = "loading")
    val loadingProgress by loadingAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading progress"
    )

    // Handle navigation
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(splashScreenDuration)
        isAnimationFinished = true
        delay(300) // Short delay before navigation
        onNavigateToNext()
    }

    // Gradient colors
    val primaryColor = Color(0xFFFF4D8D) // Vibrant pink
    val secondaryColor = Color(0xFFAF69EE) // Purple
    val accentColor = Color(0xFFFFD700) // Gold
    val darkAccent = Color(0xFF9C27B0) // Dark purple

    // Create shimmer brush
    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.4f),
        Color.White.copy(alpha = 0.0f)
    )

    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(shimmerTranslateAnim - 500, 0f),
        end = Offset(shimmerTranslateAnim, 0f)
    )

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7C0099), // Deep purple
            Color(0xFFD81B60), // Deep pink
            Color(0xFF8E24AA)  // Purple
        )
    )

    // Shimmering text gradient
    val textGradient = Brush.linearGradient(
        colors = listOf(accentColor, Color.White, accentColor),
        start = Offset(shimmerTranslateAnim - 200, 0f),
        end = Offset(shimmerTranslateAnim + 200, 0f)
    )

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Animated background particles
        for (i in 0 until 15) {
            val randomX = remember { (-400..400).random().toFloat() }
            val randomY = remember { (-800..800).random().toFloat() }
            val randomSize = remember { (20..60).random().toFloat() }
            val randomDelay = remember { (0..2000).random() }
            val randomDuration = remember { (3000..6000).random() }

            val particleAlpha by animateFloatAsState(
                targetValue = if (startAnimation) 0.5f else 0f,
                animationSpec = tween(randomDuration, delayMillis = randomDelay)
            )

            // Individual particle animation
            val individualParticleAnim = rememberInfiniteTransition(label = "particle$i")
            val particleY by individualParticleAnim.animateFloat(
                initialValue = randomY - 50f,
                targetValue = randomY + 50f,
                animationSpec = infiniteRepeatable(
                    animation = tween(randomDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particleY$i"
            )

            Box(
                modifier = Modifier
                    .offset(x = randomX.dp, y = particleY.dp)
                    .size(randomSize.dp * particleScale)
                    .alpha(particleAlpha * 0.3f)
                    .blur(radius = 8.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.6f),
                                primaryColor.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Rotating glow effect
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f)
        ) {
            rotate(rotation) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width * 0.5f
                    ),
                    center = center,
                    radius = size.minDimension * 0.5f
                )

                // Draw glowing rays
                for (angle in 0 until 360 step 45) {
                    val radians = Math.toRadians(angle.toDouble())
                    val startX = center.x
                    val startY = center.y
                    val endX = center.x + cos(radians).toFloat() * size.width * 0.5f
                    val endY = center.y + sin(radians).toFloat() * size.width * 0.5f

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.7f),
                                Color.Transparent
                            ),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 30f,
                        cap = StrokeCap.Round,
                        alpha = 0.3f
                    )
                }
            }
        }

        // Main content column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo with effects
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(12.dp)
                    .scale(logoScale)
            ) {
                // Glow effect behind logo
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .blur(radius = 20.dp)
                        .alpha(0.6f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor,
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Shadow effect
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(305.dp)
                        .offset(x = 3.dp, y = 3.dp)
                        .alpha(0.4f)
                        .blur(radius = 2.dp)
                )

                // Main logo with shimmer overlay
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Glow Girls Logo",
                        modifier = Modifier.size(300.dp)
                    )

                    // Shimmer effect overlay
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .background(shimmerBrush)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Name with fancy text effects
            Text(
                text = "GLOW GIRLS",
                textAlign = TextAlign.Center,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                style = TextStyle(
                    brush = textGradient,
                    shadow = Shadow(
                        color = darkAccent,
                        offset = Offset(1f, 1f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier
                    .alpha(textAlpha)
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline with fade-in animation
            Text(
                text = "Illuminate Your Beauty",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            // Decorative element
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .alpha(textAlpha)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White,
                                Color.White,
                                Color.Transparent
                            )
                        )
                    )
            )

            // Fancy loading indicator
            Spacer(modifier = Modifier.height(50.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .alpha(textAlpha)
                    .size(50.dp)
            ) {
                // Background circle
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                // Animated progress circle
                Canvas(modifier = Modifier.size(50.dp)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.0f),
                                primaryColor.copy(alpha = 0.5f),
                                primaryColor.copy(alpha = 0.8f),
                                accentColor
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = loadingProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                }

                // Inner circle for aesthetics
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    secondaryColor.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Dot that follows the progress
                val angle = loadingProgress * 2 * Math.PI - Math.PI/2
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(
                            x = (cos(angle) * 21).dp,
                            y = (sin(angle) * 21).dp
                        )
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onNavigateToNext = {})
}