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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glowgirls.data.budget.SpendingViewModel
import com.example.glowgirls.repository.BudgetRepository
import com.example.glowgirls.models.budget.CategoryProgress
import com.example.glowgirls.models.budget.Expense
import java.text.SimpleDateFormat
import java.util.*

// Feminine color palette
private val RosePink = Color(0xFFF8BBD0)
private val DeepPink = Color(0xFFEC407A)
private val LavenderPurple = Color(0xFFB39DDB)
private val SoftViolet = Color(0xFF9575CD)
private val PaleTeal = Color(0xFF80CBC4)
private val MintGreen = Color(0xFFA5D6A7)
private val PeachOrange = Color(0xFFFFCC80)
private val SoftYellow = Color(0xFFFFF59D)

// Background gradient
private val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFCE4EC),
        Color(0xFFF8F8F8)
    )
)

private val ChartColors = listOf(
    DeepPink,
    SoftViolet,
    PaleTeal,
    MintGreen,
    PeachOrange,
    SoftYellow
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingScreen(navController: NavController) {
    val viewModel = SpendingViewModel(BudgetRepository())
    val categoryProgress = viewModel.categoryProgress.collectAsState().value
    val expenses = viewModel.expenses.collectAsState().value
    val totalSpent = viewModel.totalSpent.collectAsState().value
    val repository = BudgetRepository()
    val suggestions by repository.getBudgetSuggestions().collectAsState(initial = emptyMap())

    // Animation state for cards
    var isLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        isLoaded = true
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
                        "Spending Overview",
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
            if (categoryProgress.isEmpty() && expenses.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Total Spent
                    item {
                        TotalSpentCard(
                            totalSpent = totalSpent,
                            elevation = cardElevation
                        )
                    }

                    // Spending Trends
                    item {
                        Text(
                            text = "Spending Trends",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepPink
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Last 7 Days",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                SpendingTrendChart(
                                    expenses = expenses,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                )
                            }
                        }
                    }

                    // Category Breakdown
                    item {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepPink
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    item {
                        SpendingPieChart(
                            categoryProgress = categoryProgress,
                            elevation = cardElevation
                        )
                    }

                    // Budget Suggestions Section
                    item {
                        Text(
                            text = "Smart Suggestions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepPink
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    item {
                        BudgetSuggestionsCard(
                            suggestions = suggestions,
                            elevation = cardElevation
                        )
                    }

                    // Recent Expenses
                    item {
                        Text(
                            text = "Recent Expenses",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepPink
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(expenses.take(5)) { expense ->
                        ExpenseItem(expense, cardElevation)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
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
                    imageVector = Icons.Rounded.Savings,
                    contentDescription = null,
                    tint = RosePink,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "No spending data available",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Add a budget and some expenses to start tracking your spending",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun TotalSpentCard(totalSpent: Double, elevation: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Spent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "KSh ${String.format("%,.2f", totalSpent)}",
                style = MaterialTheme.typography.headlineMedium,
                color = DeepPink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SpendingTrendChart(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    val days = 7
    val amounts = FloatArray(days) { 0f }
    val currentDate = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val dayLabels = mutableListOf<String>()

    // Generate day labels
    for (i in days-1 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -i)
        dayLabels.add(SimpleDateFormat("EEE", Locale.US).format(cal.time))
    }

    // Aggregate expenses by day
    expenses.forEach { expense ->
        try {
            val expenseDate = dateFormat.parse(expense.date) ?: return@forEach
            val expenseCal = Calendar.getInstance().apply { time = expenseDate }
            val daysDiff = ((currentDate.timeInMillis - expenseCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff in 0 until days) {
                amounts[days - 1 - daysDiff] += expense.amount.toFloat()
            }
        } catch (e: Exception) {
            // Handle date parsing error gracefully
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height - 30.dp.toPx() // Leave space for labels
        val maxAmount = amounts.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val stepX = width / (days - 1)
        val path = Path()

        // Draw horizontal guidelines
        val guidelineCount = 3
        for (i in 0..guidelineCount) {
            val y = height * (1 - i.toFloat() / guidelineCount)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Create gradient for the area under the line
        val gradientPath = Path()
        gradientPath.moveTo(0f, height)

        // Normalize and plot points
        amounts.forEachIndexed { index, amount ->
            val x = index * stepX
            val y = height - (amount / maxAmount) * height
            if (index == 0) {
                path.moveTo(x, y)
                gradientPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                gradientPath.lineTo(x, y)
            }
        }

        gradientPath.lineTo(width, height)
        gradientPath.close()

        // Draw the gradient area
        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    RosePink.copy(alpha = 0.5f),
                    RosePink.copy(alpha = 0.1f)
                )
            )
        )

        // Draw the path
        drawPath(
            path = path,
            color = DeepPink,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        amounts.forEachIndexed { index, amount ->
            val x = index * stepX
            val y = height - (amount / maxAmount) * height
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = DeepPink,
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )

            // Draw day labels
            drawContext.canvas.nativeCanvas.drawText(
                dayLabels[index],
                x,
                height + 20.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 12.dp.toPx()
                }
            )
        }
    }
}

@Composable
fun SpendingPieChart(
    categoryProgress: List<CategoryProgress>,
    elevation: Float
) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Pie Chart
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.Center)
                ) {
                    val totalSpent = categoryProgress.sumOf { it.spent }
                    if (totalSpent == 0.0) return@Canvas

                    var startAngle = 0f
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val innerRadius = radius * 0.6f // For donut chart effect

                    categoryProgress.forEachIndexed { index, progress ->
                        val sweepAngle = (progress.spent / totalSpent * 360).toFloat()
                        drawArc(
                            color = ChartColors[index % ChartColors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )

                        // Create donut hole
                        drawCircle(
                            color = Color.White,
                            radius = innerRadius,
                            center = center
                        )

                        startAngle += sweepAngle
                    }
                }

                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${categoryProgress.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepPink
                    )
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Legend
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryProgress.forEachIndexed { index, progress ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(ChartColors[index % ChartColors.size])
                            )
                            Text(
                                text = progress.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "KSh ${String.format("%,.2f", progress.spent)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetSuggestionsCard(
    suggestions: Map<String, Double>,
    elevation: Float
) {
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
                    imageVector = Icons.Rounded.AccountBalance,
                    contentDescription = null,
                    tint = SoftViolet
                )
                Text(
                    "Budget Suggestions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            )

            if (suggestions.isNotEmpty()) {
                suggestions.forEach { (category, suggestedAmount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowUpward,
                                contentDescription = null,
                                tint = MintGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "KSh ${String.format("%,.2f", suggestedAmount)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = DeepPink
                            )
                        )
                    }
                }
            } else {
                Text(
                    "No suggestions at this time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, elevation: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category indicator
                val categoryColor = when (expense.category.lowercase()) {
                    "food" -> DeepPink
                    "shopping" -> SoftViolet
                    "transport" -> PaleTeal
                    "entertainment" -> PeachOrange
                    "bills" -> SoftYellow
                    else -> MintGreen
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = expense.category.take(1).uppercase(),
                        color = categoryColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = try {
                                SimpleDateFormat("MMM dd", Locale.getDefault()).format(
                                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expense.date) ?: Date()
                                )
                            } catch (e: Exception) {
                                expense.date
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = "KSh ${String.format("%,.2f", expense.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = DeepPink
            )
        }
    }
}