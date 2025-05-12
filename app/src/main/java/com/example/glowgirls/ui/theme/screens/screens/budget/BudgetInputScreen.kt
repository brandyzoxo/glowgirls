package com.example.glowgirls.ui.theme.screens.screens.budget
import kotlin.math.min
import androidx.compose.material.ripple.rememberRipple

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glowgirls.repository.BudgetRepository
import com.example.glowgirls.models.budget.Budget
import com.example.glowgirls.models.budget.CategoryAllocation
import com.example.glowgirls.models.budget.CategoryAllocationWrapper
import com.example.glowgirls.models.budget.SavingsGoal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Custom colors for feminine design theme
private val RosePink = Color(0xFFF8BBD0)
private val DeepRose = Color(0xFFEC407A)
private val LightPink = Color(0xFFFCE4EC)
private val MintGreen = Color(0xFFB2DFDB)
private val Lavender = Color(0xFFD1C4E9)
private val SoftBlue = Color(0xFFBBDEFB)
private val Gold = Color(0xFFFFD700)
private val Coral = Color(0xFFFF7F50)
private val Turquoise = Color(0xFF40E0D0)
private val Purple = Color(0xFF9C27B0)

// Budget personality types
enum class BudgetPersonality(val title: String, val description: String, val color: Color) {
    SAVER("The Saver", "You're careful with money and prioritize long-term goals.", MintGreen),
    BALANCED("The Balanced", "You maintain a healthy balance between saving and spending.", Lavender),
    SPENDER("The Spender", "You live in the moment and enjoy treating yourself.", RosePink),
    INVESTOR("The Investor", "You focus on growing your wealth through smart investments.", Turquoise),
    GIVER("The Giver", "You're generous and enjoy spending on others.", Coral)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetInputScreen(
    navController: NavController,
    onBudgetSaved: () -> Unit
) {
    val repository = BudgetRepository()
    var totalAmount by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("Monthly") }
    var newCategoryName by remember { mutableStateOf("") }
    var savingsTarget by remember { mutableStateOf("") }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showBudgetTipsDialog by remember { mutableStateOf(false) }
    var showPersonalityQuiz by remember { mutableStateOf(false) }
    var selectedPersonality by remember { mutableStateOf<BudgetPersonality?>(null) }
    var showConfetti by remember { mutableStateOf(false) }

    // Smart budget assistant state
    var showAssistant by remember { mutableStateOf(false) }
    var smartSuggestions by remember { mutableStateOf(emptyList<Pair<String, Double>>()) }

    val categories = remember { mutableStateListOf<CategoryAllocationWrapper>() }
    val predefinedCategories = listOf(
        "Housing",
        "Food",
        "Transportation",
        "Entertainment",
        "Shopping",
        "Healthcare",
        "Education"
    )

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Animation for budget allocation progress
    val animatedProgress = remember { Animatable(0f) }

    // Calculate remaining balance
    val totalAmountValue = totalAmount.toDoubleOrNull() ?: 0.0
    val allocatedAmount = categories.sumOf { it.amount }
    val remainingBalance = totalAmountValue - allocatedAmount
    val allocationPercentage =
        if (totalAmountValue > 0) minOf((allocatedAmount.toFloat() / totalAmountValue.toFloat()), 1f)
        else 0f

    // Update animation target
    LaunchedEffect(allocationPercentage) {
        animatedProgress.animateTo(
            targetValue = allocationPercentage.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    // Generate smart suggestions when total amount changes and we have no categories yet
    LaunchedEffect(totalAmount) {
        if (totalAmount.toDoubleOrNull() ?: 0.0 > 0 && categories.isEmpty()) {
            val total = totalAmount.toDoubleOrNull() ?: 0.0
            smartSuggestions = generateSmartSuggestions(total, period)
            if (smartSuggestions.isNotEmpty()) {
                delay(500)
                showAssistant = true
            }
        }
    }

    // Confetti animation effect
    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(3000)
            showConfetti = false
        }
    }

    // Automatically add savings as a category if target is set
    val savingsTargetValue = savingsTarget.toDoubleOrNull() ?: 0.0
    if (savingsTargetValue > 0 && !categories.any {
            it.name.equals(
                "Savings",
                ignoreCase = true
            )
        }) {
        categories.add(CategoryAllocationWrapper("Savings", savingsTargetValue))
    } else if (savingsTargetValue <= 0 && categories.any {
            it.name.equals(
                "Savings",
                ignoreCase = true
            )
        }) {
        categories.removeIf { it.name.equals("Savings", ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightPink,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBudgetTipsDialog = true },
                containerColor = DeepRose,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = "Budget Tips")
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                LightPink,
                                Color.White
                            )
                        )
                    )
            ) {
                // Confetti Animation when budget is complete
                if (showConfetti) {
                    ConfettiAnimation(modifier = Modifier.fillMaxSize())
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header with budget personality
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Create Your Budget",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = DeepRose,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    "Plan your finances with style",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Budget personality badge
                            if (selectedPersonality != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(selectedPersonality!!.color.copy(alpha = 0.15f))
                                        .border(1.dp, selectedPersonality!!.color, RoundedCornerShape(20.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = LocalIndication.current
                                        ) {
                                            showPersonalityQuiz = true
                                        }

                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Face,  // Or another icon representing personality
                                            contentDescription = null,
                                            tint = selectedPersonality!!.color,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            selectedPersonality!!.title,
                                            color = Color.DarkGray,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { showPersonalityQuiz = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DeepRose.copy(alpha = 0.1f),
                                        contentColor = DeepRose
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            "Find Your Style",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Budget Period and Amount Card
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Budget Details",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = DeepRose,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                // Updated Period Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = false, // Controlled by internal state
                                    onExpandedChange = {}, // Handled by custom composable
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    PeriodDropdownField(
                                        selectedOption = period,
                                        onOptionSelected = { period = it },
                                        modifier = Modifier.menuAnchor()
                                    )
                                }

                                // Total Budget Input
                                OutlinedTextField(
                                    value = totalAmount,
                                    onValueChange = { totalAmount = it },
                                    label = { Text("Total Budget Amount") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = {
//                                        Icon(
//                                            Icons.Filled.AttachMoney,
//                                            contentDescription = null,
//                                            tint = DeepRose
//                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepRose,
                                        focusedLabelColor = DeepRose,
                                        focusedLeadingIconColor = DeepRose
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Budget Progress Visualization
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                BudgetProgressChart(
                                    progress = animatedProgress.value,
                                    categories = categories,
                                    totalAmount = totalAmountValue
                                )
                                Text(
                                    buildString {
                                        append("Budget Allocated: ")
                                        append(String.format("%.1f", allocationPercentage * 100))
                                        append("%")
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )

                            }
                        }
                    }

                    // Savings Goal Card
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Lavender.copy(alpha = 0.4f)
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
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
                                        Icons.Outlined.Savings,
                                        contentDescription = null,
                                        tint = DeepRose
                                    )
                                    Text(
                                        "Set Savings Goal",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = DeepRose,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                OutlinedTextField(
                                    value = savingsTarget,
                                    onValueChange = { savingsTarget = it },
                                    label = { Text("Target Amount") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepRose,
                                        focusedLabelColor = DeepRose
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Savings Suggestion
                                if (totalAmountValue > 0) {
                                    val suggestedSavings = totalAmountValue * 0.2 // 20% rule
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Recommended (20%): KSh ${
                                                String.format(
                                                    "%.2f",
                                                    suggestedSavings
                                                )
                                            }",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )

                                        TextButton(
                                            onClick = {
                                                savingsTarget = suggestedSavings.toString()
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = DeepRose
                                            )
                                        ) {
                                            Text("Apply", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Balance Display Card
                    item {
                        val isPositiveBalance = remainingBalance >= 0
                        val balanceColor = if (isPositiveBalance) MintGreen else RosePink
                        val textColor =
                            if (isPositiveBalance) Color(0xFF2E7D32) else Color(0xFFD32F2F)

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = balanceColor.copy(alpha = 0.7f)
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Remaining Balance",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "KSh ${String.format("%.2f", remainingBalance)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )

                                // Show suggestion if balance is positive
                                if (isPositiveBalance && remainingBalance > totalAmountValue * 0.1) {
                                    Text(
                                        "Tip: Consider allocating remaining funds to savings or investments",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2E7D32),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Smart Budget Assistant Card (Animated)
                    item {
                        AnimatedVisibility(
                            visible = showAssistant && smartSuggestions.isNotEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = Turquoise.copy(alpha = 0.15f)
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Outlined.Assistant,
                                                contentDescription = null,
                                                tint = DeepRose
                                            )
                                            Text(
                                                "Smart Budget Assistant",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = DeepRose,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        IconButton(
                                            onClick = { showAssistant = false },
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Close Assistant",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        "I've created a personalized budget suggestion based on your income. Would you like to apply it?",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        smartSuggestions.forEach { (category, amount) ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    category,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    "KSh ${String.format("%.2f", amount)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { showAssistant = false },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = DeepRose
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text("No Thanks")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                // Apply the smart suggestions to the categories
                                                categories.clear()
                                                smartSuggestions.forEach { (category, amount) ->
                                                    categories.add(
                                                        CategoryAllocationWrapper(
                                                            category,
                                                            amount
                                                        )
                                                    )
                                                }
                                                showAssistant = false
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = DeepRose
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text("Apply Suggestion")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Categories Section
                    // Add Categories Section
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween, // Improved alignment
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Category,
                                            contentDescription = null,
                                            tint = DeepRose
                                        )
                                        Text(
                                            "Budget Categories",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = DeepRose,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    FilledIconButton(
                                        onClick = { showAddCategoryDialog = true },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = DeepRose.copy(alpha = 0.8f)
                                        ),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Category",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Quick add popular categories
                                if (categories.isEmpty()) {
                                    QuickCategoryChips(
                                        suggestions = predefinedCategories,
                                        onCategorySelected = { category ->
                                            if (!categories.any { it.name == category }) {
                                                categories.add(CategoryAllocationWrapper(category))
                                            }
                                        }
                                    )
                                }

                                if (categories.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No categories added yet.\nTap + to add your first category or select from the suggestions above.",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    categories.forEachIndexed { index, category ->
                                        CategoryAllocationItem(
                                            category = category,
                                            onAmountChanged = { amount ->
                                                categories[index] = category.copy(amount = amount)
                                            },
                                            onDelete = {
                                                categories.removeAt(index)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Save Button
                    item {
                        Button(
                            onClick = {
                                println("Save Budget clicked") // Debug log
                                val total = totalAmount.toDoubleOrNull() ?: 0.0
                                if (total <= 0) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please enter a valid budget amount")
                                    }
                                    return@Button
                                }

                                if (categories.isEmpty()) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please add at least one category")
                                    }
                                    return@Button
                                }

                                val allocatedSum = categories.sumOf { it.amount }
                                if (allocatedSum > total) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Allocated amounts exceed total budget")
                                    }
                                    return@Button
                                }

                                val budget = Budget(
                                    period = period.lowercase(),
                                    totalAmount = total,
                                    startDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
                                        Date()
                                    ),
                                    categories = categories.associate {
                                        it.name to CategoryAllocation(allocatedAmount = it.amount)
                                    },
                                    savingsGoal = if (savingsTargetValue > 0) {
                                        SavingsGoal(
                                            targetAmount = savingsTargetValue,
                                            currentAmount = 0.0
                                        )
                                    } else null
                                )

                                coroutineScope.launch {
                                    try {
                                        repository.saveBudget(budget).onSuccess {
                                            println("Budget saved successfully") // Debug log
                                            showConfetti = true
                                            snackbarHostState.showSnackbar("Budget saved successfully! 🎉")
                                            onBudgetSaved()
                                        }.onFailure { exception ->
                                            println("Failed to save budget: ${exception.message}") // Debug log
                                            snackbarHostState.showSnackbar("Failed to save budget: ${exception.message}")
                                        }
                                    } catch (e: Exception) {
                                        println("Exception in saveBudget: ${e.message}") // Debug log
                                        snackbarHostState.showSnackbar("Error saving budget")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = remainingBalance >= 0 && categories.isNotEmpty() && totalAmount.isNotEmpty(),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepRose,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                "Save Budget",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    )

    // Add Category Dialog
    if (showAddCategoryDialog) {
        EnhancedAddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            predefinedCategories = predefinedCategories,
            existingCategories = categories.map { it.name },
            onCategoryAdded = { categoryName ->
                if (categoryName.isNotBlank()) {
                    if (categories.any { it.name.equals(categoryName, ignoreCase = true) }) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Category already exists")
                        }
                    } else {
                        categories.add(CategoryAllocationWrapper(categoryName.trim()))
                    }
                }
                showAddCategoryDialog = false
            }
        )
    }

    // Budget Tips Dialog
    if (showBudgetTipsDialog) {
        BudgetTipsDialog(onDismiss = { showBudgetTipsDialog = false })
    }

    // Budget Personality Quiz Dialog
    if (showPersonalityQuiz) {
        BudgetPersonalityQuizDialog(
            onDismiss = { showPersonalityQuiz = false },
            onPersonalitySelected = { personality ->
                selectedPersonality = personality
                showPersonalityQuiz = false
            }
        )
    }
}

// Helper Functions

@Composable
fun BudgetProgressChart(
    progress: Float,
    categories: List<CategoryAllocationWrapper>,
    totalAmount: Double
) {
    val sweepAngle = 360 * progress

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2.2f
            val strokeWidth = 20f

            // Draw background circle
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(
                    (size.width - canvasSize) / 2 + strokeWidth / 2,
                    (size.height - canvasSize) / 2 + strokeWidth / 2
                ),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Draw progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        MintGreen,
                        SoftBlue,
                        Lavender,
                        DeepRose,
                        RosePink
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    (size.width - canvasSize) / 2 + strokeWidth / 2,
                    (size.height - canvasSize) / 2 + strokeWidth / 2
                ),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.0f%%", progress * 100),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DeepRose
            )
            Text(
                text = "Allocated",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ConfettiAnimation(modifier: Modifier = Modifier) {
    // Placeholder animation with squares
    Box(modifier = modifier) {
        val infiniteTransition = rememberInfiniteTransition()
        val colors = listOf(RosePink, DeepRose, MintGreen, Lavender, SoftBlue, Gold, Coral)

        repeat(30) { index ->
            val xOffset by infiniteTransition.animateFloat(
                initialValue = (index * 33) % 100 / 100f,
                targetValue = ((index * 33) % 100 / 100f + 1f) % 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, delayMillis = index * 50),
                    repeatMode = RepeatMode.Restart
                )
            )

            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000 + index * 50),
                    repeatMode = RepeatMode.Restart
                )
            )

            val color = colors[index % colors.size]
            val size = ((index % 4 + 1) * 8).dp

            Box(
                modifier = Modifier
                    .offset(
                        x = (xOffset * LocalConfiguration.current.screenWidthDp).dp,
                        y = (yOffset * LocalConfiguration.current.screenHeightDp).dp
                    )
                    .size(size)
                    .background(color, RoundedCornerShape(((index % 3 + 1) * 4).dp))
            )
        }
    }
}

@Composable
fun BudgetTipsDialog(onDismiss: () -> Unit) {
    val tips = listOf(
        "50/30/20 Rule: 50% needs, 30% wants, 20% savings",
        "Set automated transfers to your savings account on payday",
        "Use the envelope method for discretionary spending",
        "Track all expenses, even small ones, they add up quickly",
        "Review your budget regularly and adjust as needed",
        "Pay yourself first - prioritize saving before other expenses",
        "Plan for irregular expenses like car maintenance and gifts",
        "Find a budgeting style that matches your personality",
        "Cut one unnecessary expense each month and save the difference",
        "Set clear, achievable financial goals to stay motivated"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Smart Budget Tips",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DeepRose,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            LazyColumn {
                items(tips.size) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "${index + 1}.",
                            fontWeight = FontWeight.Bold,
                            color = DeepRose
                        )
                        Text(tips[index])
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRose
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Got it!")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun BudgetPersonalityQuizDialog(
    onDismiss: () -> Unit,
    onPersonalitySelected: (BudgetPersonality) -> Unit
) {
    var currentQuestion by remember { mutableStateOf(0) }
    var saverScore by remember { mutableStateOf(0) }
    var spenderScore by remember { mutableStateOf(0) }
    var investorScore by remember { mutableStateOf(0) }
    var balancedScore by remember { mutableStateOf(0) }
    var giverScore by remember { mutableStateOf(0) }

    val questions = listOf(
        "When I receive money unexpectedly, I usually:",
        "When it comes to making purchases, I:",
        "My approach to long-term financial planning is:",
        "When friends suggest going out to an expensive dinner:",
        "I feel most satisfied when I:"
    )

    val options = listOf(
        listOf(
            "Save most or all of it" to "saver",
            "Spend some and save some" to "balanced",
            "Treat myself to something nice" to "spender",
            "Look for investment opportunities" to "investor",
            "Share it with friends or family" to "giver"
        ),
        listOf(
            "Research thoroughly and wait for sales" to "saver",
            "Balance cost with quality and need" to "balanced",
            "Buy what I want when I want it" to "spender",
            "Consider the long-term value and return" to "investor",
            "Think about what others might need too" to "giver"
        ),
        listOf(
            "Have detailed savings goals and plans" to "saver",
            "Save regularly but leave room for flexibility" to "balanced",
            "Live in the moment, the future will work itself out" to "spender",
            "Focus on building wealth through investments" to "investor",
            "Plan to help others as well as myself" to "giver"
        ),
        listOf(
            "Suggest a cheaper alternative" to "saver",
            "Go occasionally if it fits my budget" to "balanced",
            "I'm usually the one suggesting it!" to "spender",
            "Calculate if it's worth the opportunity cost" to "investor",
            "Offer to treat someone else if I can" to "giver"
        ),
        listOf(
            "See my savings account grow" to "saver",
            "Maintain a healthy financial balance" to "balanced",
            "Experience new things and enjoy life" to "spender",
            "See my investments increase in value" to "investor",
            "Help others achieve their goals" to "giver"
        )
    )

    // Function to determine personality based on scores
    fun determinePersonality(): BudgetPersonality {
        val scores = mapOf(
            BudgetPersonality.SAVER to saverScore,
            BudgetPersonality.BALANCED to balancedScore,
            BudgetPersonality.SPENDER to spenderScore,
            BudgetPersonality.INVESTOR to investorScore,
            BudgetPersonality.GIVER to giverScore
        )

        return scores.maxByOrNull { it.value }?.key ?: BudgetPersonality.BALANCED
    }

    // Process answer
    fun processAnswer(personalityType: String) {
        when (personalityType) {
            "saver" -> saverScore++
            "balanced" -> balancedScore++
            "spender" -> spenderScore++
            "investor" -> investorScore++
            "giver" -> giverScore++
        }

        if (currentQuestion < questions.size - 1) {
            currentQuestion++
        } else {
            onPersonalitySelected(determinePersonality())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "What's Your Budget Personality?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DeepRose,
                        fontWeight = FontWeight.Bold
                    )
                )
                LinearProgressIndicator(
                    progress = (currentQuestion.toFloat() / questions.size),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    color = DeepRose
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    questions[currentQuestion],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                options[currentQuestion].forEach { (option, personalityType) ->
                    Button(
                        onClick = { processAnswer(personalityType) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightPink,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DeepRose
                )
            ) {
                Text("Skip")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun EnhancedAddCategoryDialog(
    onDismiss: () -> Unit,
    predefinedCategories: List<String>,
    existingCategories: List<String>,
    onCategoryAdded: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCategories = predefinedCategories
        .filter { it.contains(searchQuery, ignoreCase = true) }
        .filter { predefined ->
            !existingCategories.any {
                it.equals(
                    predefined,
                    ignoreCase = true
                )
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add New Category",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DeepRose,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepRose,
                        focusedLabelColor = DeepRose
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (predefinedCategories.isNotEmpty()) {
                    Text(
                        "Or select from suggestions:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search categories") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepRose,
                            focusedLabelColor = DeepRose
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = DeepRose
                            )
                        }
                    )

                    QuickCategoryChips(
                        suggestions = filteredCategories,
                        onCategorySelected = {
                            categoryName = it
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCategoryAdded(categoryName) },
                enabled = categoryName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRose
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DeepRose
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
fun QuickCategoryChips(
    suggestions: List<String>,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { category ->
            SuggestionChip(
                modifier = Modifier.padding(vertical = 4.dp),
                onClick = { onCategorySelected(category) },
                label = {
                    Text(text = category)
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = LightPink.copy(alpha = 0.6f),
                    labelColor = DeepRose
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodDropdownField(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Weekly", "Monthly", "Yearly")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            label = { Text("Budget Period") },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = DeepRose
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepRose,
                focusedLabelColor = DeepRose
            ),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryAllocationItem(
    category: CategoryAllocationWrapper,
    onAmountChanged: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var amountText by remember { mutableStateOf(category.amount.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            category.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { newValue ->
                amountText = newValue
                val amount = newValue.toDoubleOrNull() ?: 0.0
                onAmountChanged(amount)
            },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepRose,
                focusedLabelColor = DeepRose
            ),
            shape = RoundedCornerShape(12.dp)
        )

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Category",
                tint = DeepRose
            )
        }
    }
}

// Function to generate smart budget suggestions based on income
fun generateSmartSuggestions(totalAmount: Double, period: String): List<Pair<String, Double>> {
    val suggestions = mutableListOf<Pair<String, Double>>()

    // Default allocation percentages based on 50/30/20 rule with some customizations
    // Housing: 30%, Food: 15%, Transportation: 10%, Utilities: 5%, Savings: 20%, Entertainment: 10%, Shopping: 5%, Others: 5%
    val multiplier = when (period) {
        "Weekly" -> 0.25
        "Yearly" -> 12.0
        else -> 1.0 // Monthly
    }

    suggestions.add("Housing" to totalAmount * 0.30 * multiplier)
    suggestions.add("Food" to totalAmount * 0.15 * multiplier)
    suggestions.add("Transportation" to totalAmount * 0.10 * multiplier)
    suggestions.add("Utilities" to totalAmount * 0.05 * multiplier)
    suggestions.add("Savings" to totalAmount * 0.20 * multiplier)
    suggestions.add("Entertainment" to totalAmount * 0.10 * multiplier)
    suggestions.add("Shopping" to totalAmount * 0.05 * multiplier)
    suggestions.add("Others" to totalAmount * 0.05 * multiplier)

    return suggestions
}