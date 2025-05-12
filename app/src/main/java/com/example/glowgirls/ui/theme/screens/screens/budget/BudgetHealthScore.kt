package com.example.glowgirls.ui.theme.screens.screens.budget


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glowgirls.models.budget.Budget
import com.example.glowgirls.models.budget.Expense
import com.example.glowgirls.repository.BudgetRepository
import kotlinx.coroutines.flow.first

// Feminine color palette (reusing from SpendingScreen)
private val RosePink = Color(0xFFF8BBD0)
private val DeepPink = Color(0xFFEC407A)
private val LavenderPurple = Color(0xFFB39DDB)
private val SoftViolet = Color(0xFF9575CD)
private val PaleTeal = Color(0xFF80CBC4)
private val MintGreen = Color(0xFFA5D6A7)

// Background gradient
private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFCE4EC),
        Color(0xFFF8F8F8)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetHealthScreen(navController: NavController) {
    val repository = BudgetRepository()
    var budget by remember { mutableStateOf<Budget?>(null) }
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var healthScore by remember { mutableStateOf(0) }
    var insights by remember { mutableStateOf<List<String>>(emptyList()) }

    // Animation state for score
    var animatedScore by remember { mutableStateOf(0f) }
    val animatedScoreValue by animateFloatAsState(
        targetValue = animatedScore,
        animationSpec = tween(1500)
    )

    // Animation state for cards
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        isLoaded = true

        // Fetch the latest budget and expenses
        budget = repository.getLatestBudget().first()
        expenses = repository.getExpenses().first()

        // Calculate health score and insights
        healthScore = repository.calculateBudgetHealthScore(budget, expenses)
        insights = repository.generateFinancialInsights(budget, expenses)

        // Start score animation
        animatedScore = healthScore.toFloat()
    }

    val cardElevation by animateFloatAsState(
        targetValue = if (isLoaded) 4f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Budget Health",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RosePink,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundGradient)
        ) {
            if (budget == null && expenses.isEmpty()) {
                // Show empty state if no data
                EmptyBudgetHealthState()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Health Score Card
                    item {
                        HealthScoreCard(
                            score = animatedScoreValue.toInt(),
                            elevation = cardElevation
                        )
                    }

                    // Financial Insights
                    item {
                        Text(
                            text = "Financial Insights",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepPink
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (insights.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No insights available yet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        items(insights) { insight ->
                            InsightCard(insight = insight, elevation = cardElevation)
                        }
                    }

                    // Score Breakdown
                    item {
                        Text(
                            text = "Score Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepPink
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    item {
                        ScoreBreakdownCard(
                            budget = budget,
                            expenses = expenses,
                            elevation = cardElevation
                        )
                    }

                    // What can be improved
                    item {
                        Text(
                            text = "Improve Your Score",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepPink
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    item {
                        ImproveScoreCard(
                            healthScore = healthScore,
                            elevation = cardElevation
                        )
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBudgetHealthState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    tint = RosePink,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "No budget data available",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Create a budget and add expenses to see your financial health score",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HealthScoreCard(score: Int, elevation: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Budget Health Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Health Score Gauge
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background arc
                Canvas(modifier = Modifier.size(180.dp)) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Score arc - color based on score
                val scoreColor = when {
                    score >= 80 -> MintGreen
                    score >= 60 -> PaleTeal
                    score >= 40 -> Color.Yellow
                    else -> DeepPink
                }

                Canvas(modifier = Modifier.size(180.dp)) {
                    val sweepAngle = 270f * (score / 100f)
                    drawArc(
                        color = scoreColor,
                        startAngle = 135f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Score value
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )

                    Text(
                        text = "/" + "100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score interpretation
            val healthStatus = when {
                score >= 80 -> "Excellent"
                score >= 70 -> "Very Good"
                score >= 60 -> "Good"
                score >= 50 -> "Fair"
                score >= 40 -> "Needs Attention"
                else -> "Critical"
            }

            val statusColor = when {
                score >= 80 -> MintGreen
                score >= 60 -> PaleTeal
                score >= 40 -> Color.Yellow
                else -> DeepPink
            }

            Text(
                text = healthStatus,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            Text(
                text = getScoreDescription(score),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun InsightCard(insight: String, elevation: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = SoftViolet,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ScoreBreakdownCard(budget: Budget?, expenses: List<Expense>, elevation: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calculate metrics for breakdown
            val totalBudgeted = budget?.categories?.values?.sumOf { it.allocatedAmount } ?: 0.0
            val totalSpent = budget?.categories?.values?.sumOf { it.spentAmount } ?: 0.0
            val utilizationRatio = if (totalBudgeted > 0) totalSpent / totalBudgeted else 1.0
            val overBudgetCategories = budget?.categories?.count {
                it.value.allocatedAmount > 0 && it.value.spentAmount > it.value.allocatedAmount
            } ?: 0
            val totalCategories = budget?.categories?.size ?: 0

            // Overall Budget Usage
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Budget Usage",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    val utilizationPercent = (utilizationRatio * 100).toInt().coerceAtMost(200)
                    Text(
                        text = "$utilizationPercent%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            utilizationRatio <= 0.85 -> MintGreen
                            utilizationRatio <= 1.0 -> PaleTeal
                            utilizationRatio <= 1.1 -> Color.Yellow
                            else -> DeepPink
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = utilizationRatio.toFloat().coerceAtMost(2f) / 2f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        utilizationRatio <= 0.85 -> MintGreen
                        utilizationRatio <= 1.0 -> PaleTeal
                        utilizationRatio <= 1.1 -> Color.Yellow
                        else -> DeepPink
                    },
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }

            Divider()

            // Categories Over Budget
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories Over Budget",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    if (totalCategories > 0) {
                        Text(
                            text = "$overBudgetCategories/$totalCategories",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                overBudgetCategories == 0 -> MintGreen
                                overBudgetCategories <= totalCategories / 3 -> PaleTeal
                                overBudgetCategories <= 2 * totalCategories / 3 -> Color.Yellow
                                else -> DeepPink
                            }
                        )
                    } else {
                        Text(
                            text = "N/A",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar for categories
                if (totalCategories > 0) {
                    LinearProgressIndicator(
                        progress = overBudgetCategories.toFloat() / totalCategories,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            overBudgetCategories == 0 -> MintGreen
                            overBudgetCategories <= totalCategories / 3 -> PaleTeal
                            overBudgetCategories <= 2 * totalCategories / 3 -> Color.Yellow
                            else -> DeepPink
                        },
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                } else {
                    LinearProgressIndicator(
                        progress = 0f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.Gray,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }

            Divider()

            // Consistency Score
            val expensesByDate = expenses
                .filter { it.date.isNotBlank() }
                .groupBy { it.date.split("-").take(2).joinToString("-") }
                .mapValues { it.value.sumOf { expense -> expense.amount } }

            val consistencyScore = if (expensesByDate.size >= 2) {
                val values = expensesByDate.values.toList()
                val avg = values.average()
                val variance = values.sumOf { (it - avg).pow(2) } / values.size
                val stdDev = kotlin.math.sqrt(variance)
                val coeffVar = if (avg > 0) stdDev / avg else 1.0

                when {
                    coeffVar < 0.2 -> 100
                    coeffVar < 0.4 -> 75
                    coeffVar < 0.6 -> 50
                    else -> 25
                }
            } else {
                50
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending Consistency",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${consistencyScore.toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            consistencyScore >= 80 -> MintGreen
                            consistencyScore >= 60 -> PaleTeal
                            consistencyScore >= 40 -> Color.Yellow
                            else -> DeepPink
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar for consistency
                LinearProgressIndicator(
                    progress = consistencyScore.toFloat() / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        consistencyScore >= 80 -> MintGreen
                        consistencyScore >= 60 -> PaleTeal
                        consistencyScore >= 40 -> Color.Yellow
                        else -> DeepPink
                    },
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun ImproveScoreCard(healthScore: Int, elevation: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Insights,
                    contentDescription = null,
                    tint = SoftViolet
                )
                Text(
                    "How to Improve",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            // Different improvement suggestions based on score
            val improvements = when {
                healthScore >= 80 -> listOf(
                    "Keep maintaining your excellent budget habits",
                    "Consider investing any extra savings",
                    "Review your budget occasionally to ensure it still aligns with your financial goals"
                )
                healthScore >= 60 -> listOf(
                    "Stay within budget for all categories",
                    "Reduce the number of categories that exceed their allocated amounts",
                    "Maintain consistent spending patterns throughout the month"
                )
                healthScore >= 40 -> listOf(
                    "Focus on staying within budget for major expense categories",
                    "Adjust budgets for consistently overbudget categories",
                    "Spread large purchases throughout the month",
                    "Review and cut down on frequent small expenses"
                )
                else -> listOf(
                    "Create realistic budget allocations based on your actual spending",
                    "Track all expenses diligently",
                    "Focus on reducing spending in your highest overspent categories",
                    "Avoid large unplanned purchases",
                    "Consider using cash envelopes for problem spending areas"
                )
            }

            improvements.forEach { improvement ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(top = 8.dp)
                            .clip(CircleShape)
                            .background(MintGreen)
                    )

                    Text(
                        text = improvement,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun getScoreDescription(score: Int): String {
    return when {
        score >= 80 -> "Your budget is well-managed with healthy spending habits"
        score >= 70 -> "You're doing well with your budget, with a few areas to improve"
        score >= 60 -> "Your budget is on track but needs some attention"
        score >= 50 -> "Your budget needs attention in several key areas"
        score >= 40 -> "Your spending habits require significant adjustments"
        else -> "Your budget is in critical condition and needs immediate attention"
    }
}

private fun Double.pow(n: Int): Double {
    var result = 1.0
    for (i in 1..n) {
        result *= this
    }
    return result
}