package com.example.glowgirls.ui.theme.screens.screens.budget

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.navigation.NavController
import com.example.glowgirls.repository.BudgetRepository
import com.example.glowgirls.models.budget.Expense
import com.example.glowgirls.navigation.ROUTE_SPENDING_SCREEN
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BudgetOverviewScreen(
    navController: NavController
) {
    val repository = BudgetRepository()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Enhanced feminine color palette with sparkle colors
    val primaryPink = Color(0xFFFf5f9e)
    val softPeach = Color(0xFFFfced3)
    val accentPurple = Color(0xFFd8b4fe)
    val shimmerGold = Color(0xFFFFD700)
    val lavender = Color(0xFFE6E6FA)
    val pastelMint = Color(0xFFCDFADB)

    // Dynamic gradient with animated shift
    val infiniteTransition = rememberInfiniteTransition(label = "backgroundTransition")
    val animatedDegree = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "gradientRotation"
    )

    val gradient = Brush.linearGradient(
        0f to primaryPink.copy(alpha = 0.8f),
        0.5f to softPeach.copy(alpha = 0.6f),
        1f to accentPurple.copy(alpha = 0.7f),
        start = Offset.Zero,
        end = Offset.Infinite,
        tileMode = TileMode.Clamp
    )

    // Collect budget, expenses, and suggestions
    val budgetState by repository.getLatestBudget().collectAsState(initial = null)
    val expensesState by repository.getExpenses().collectAsState(initial = emptyList())

    // Expense input states
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    // Category edit states
    var editingCategory by remember { mutableStateOf<String?>(null) }
    var newAllocatedAmount by remember { mutableStateOf("") }

    // Quick Action FAB state
    var expandedFAB by remember { mutableStateOf(false) }

    // Show confetti animation
    var showConfetti by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = expandedFAB,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = { navController.navigate(ROUTE_SPENDING_SCREEN) },
                            containerColor = lavender,
                            contentColor = primaryPink
                        ) {
                            Icon(Icons.Default.Assessment, "View Spending")
                        }

                        SmallFloatingActionButton(
                            onClick = {
                                budgetState?.let { budget ->
                                    // Construct shareable summary
                                    val summary = buildString {
                                        append("✨ My Glow Budget ✨\n\n")
                                        append("Period: ${budget.period}\n")
                                        append("Total: KSh ${String.format("%.2f", budget.totalAmount)}\n\n")
                                        budget.categories.forEach { (category, details) ->
                                            append("💖 $category\n")
                                            append("  Allocated: KSh ${String.format("%.2f", details.allocatedAmount)}\n")
                                            append("  Spent: KSh ${String.format("%.2f", details.spentAmount)}\n")
                                            append("  Remaining: KSh ${String.format("%.2f", details.allocatedAmount - details.spentAmount)}\n\n")
                                        }
                                        append("Powered by GlowGirls Budget App ✨")
                                    }

                                    val shareIntent = ShareCompat.IntentBuilder(context)
                                        .setType("text/plain")
                                        .setSubject("My Glow Budget Overview")
                                        .setText(summary)
                                        .intent

                                    context.startActivity(shareIntent)
                                } ?: run {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("No budget available to share")
                                    }
                                }
                            },
                            containerColor = pastelMint,
                            contentColor = primaryPink
                        ) {
                            Icon(Icons.Default.Share, "Share Budget")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FloatingActionButton(
                    onClick = { expandedFAB = !expandedFAB },
                    containerColor = primaryPink,
                    contentColor = Color.White
                ) {
                    Icon(
                        if (expandedFAB) Icons.Default.Close else Icons.Rounded.Add,
                        contentDescription = "Quick Actions"
                    )
                }
            }
        },
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Background with subtle animated pattern
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(gradient)

                    // Draw subtle pattern
                    val dotSize = 2.dp.toPx()
                    val spacing = 35.dp.toPx()
                    val rows = (size.height / spacing).toInt()
                    val cols = (size.width / spacing).toInt()

                    for (row in 0..rows) {
                        for (col in 0..cols) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.15f),
                                radius = dotSize,
                                center = Offset(col * spacing, row * spacing)
                            )
                        }
                    }
                }

                // Confetti animation
                AnimatedVisibility(
                    visible = showConfetti,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ConfettiAnimation(
                            onAnimationFinish = { showConfetti = false }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App Title with Sparkle Effect
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Glow Budget",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 32.sp
                                ),
                                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                            )

                            // Animated sparkle
                            SparkleEffect(primaryColor = shimmerGold)
                        }
                    }

                    // Budget Summary Card with Progress Visualization
                    item {
                        budgetState?.let { budget ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Budget Overview",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = primaryPink,
                                            fontSize = 24.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Budget Summary Stats
                                    val totalAllocated = budget.categories.values.sumOf { it.allocatedAmount }
                                    val totalSpent = budget.categories.values.sumOf { it.spentAmount }
                                    val percentSpent = if (totalAllocated > 0) (totalSpent / totalAllocated) * 100 else 0.0

                                    BudgetProgressIndicator(
                                        percentSpent = percentSpent.toFloat(),
                                        primaryColor = primaryPink,
                                        secondaryColor = accentPurple
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Budget details
                                    budget.categories.forEach { (category, details) ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                        ) {
                                            if (editingCategory == category) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = newAllocatedAmount,
                                                        onValueChange = { newAllocatedAmount = it },
                                                        label = { Text("New Amount", color = primaryPink) },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .border(1.dp, accentPurple, RoundedCornerShape(8.dp)),
                                                        colors = TextFieldDefaults.run {
                                                            outlinedTextFieldColors(
                                                                focusedBorderColor = accentPurple,
                                                                unfocusedBorderColor = softPeach
                                                            )
                                                        },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            val newAmount = newAllocatedAmount.toDoubleOrNull() ?: return@IconButton
                                                            if (newAmount >= 0) {
                                                                val updatedBudget = budget.copy(
                                                                    categories = budget.categories.mapValues {
                                                                        if (it.key == category) it.value.copy(allocatedAmount = newAmount)
                                                                        else it.value
                                                                    }
                                                                )
                                                                coroutineScope.launch {
                                                                    repository.saveBudget(updatedBudget).onSuccess {
                                                                        snackbarHostState.showSnackbar("Category updated successfully")
                                                                        editingCategory = null
                                                                        newAllocatedAmount = ""
                                                                        showConfetti = true
                                                                    }.onFailure {
                                                                        snackbarHostState.showSnackbar("Failed to update category")
                                                                    }
                                                                }
                                                            } else {
                                                                coroutineScope.launch {
                                                                    snackbarHostState.showSnackbar("Amount must be non-negative")
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .padding(start = 8.dp)
                                                            .clip(CircleShape)
                                                            .background(accentPurple)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Save,
                                                            contentDescription = "Save",
                                                            tint = Color.White
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            editingCategory = null
                                                            newAllocatedAmount = ""
                                                        },
                                                        modifier = Modifier
                                                            .padding(start = 8.dp)
                                                            .clip(CircleShape)
                                                            .background(primaryPink)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Cancel",
                                                            tint = Color.White
                                                        )
                                                    }
                                                }
                                            } else {
                                                val percentCategorySpent = if (details.allocatedAmount > 0)
                                                    (details.spentAmount / details.allocatedAmount) * 100 else 0.0

                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (percentCategorySpent >= 90)
                                                            softPeach else lavender.copy(alpha = 0.7f)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                category,
                                                                style = MaterialTheme.typography.titleMedium.copy(
                                                                    color = primaryPink,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            )

                                                            // Horizontal progress bar
                                                            LinearProgressIndicator(
                                                                progress = (percentCategorySpent / 100).toFloat(),
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(8.dp)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .padding(vertical = 2.dp),
                                                                color = when {
                                                                    percentCategorySpent > 90 -> Color.Red
                                                                    percentCategorySpent > 75 -> primaryPink
                                                                    else -> accentPurple
                                                                },
                                                                trackColor = softPeach
                                                            )

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(
                                                                    "KSh ${String.format("%.2f", details.spentAmount)}",
                                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                                        color = Color.Black.copy(alpha = 0.6f)
                                                                    )
                                                                )

                                                                Text(
                                                                    "KSh ${String.format("%.2f", details.allocatedAmount)}",
                                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                                        color = Color.Black.copy(alpha = 0.6f)
                                                                    )
                                                                )
                                                            }

                                                            val remaining = details.allocatedAmount - details.spentAmount
                                                            Text(
                                                                "Remaining: KSh ${String.format("%.2f", remaining)}",
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    color = if (remaining >= 0) accentPurple else primaryPink,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            )
                                                        }

                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            IconButton(
                                                                onClick = {
                                                                    editingCategory = category
                                                                    newAllocatedAmount = details.allocatedAmount.toString()
                                                                },
                                                                modifier = Modifier
                                                                    .size(36.dp)
                                                                    .clip(CircleShape)
                                                                    .background(accentPurple.copy(alpha = 0.2f))
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Edit,
                                                                    contentDescription = "Edit Category",
                                                                    tint = accentPurple,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                            IconButton(
                                                                onClick = {
                                                                    coroutineScope.launch {
                                                                        repository.deleteCategoryFromBudget(
                                                                            budgetId = budget.id,
                                                                            categoryName = category
                                                                        ).onSuccess {
                                                                            snackbarHostState.showSnackbar("Category deleted successfully")
                                                                        }.onFailure {
                                                                            snackbarHostState.showSnackbar("Failed to delete category")
                                                                        }
                                                                    }
                                                                },
                                                                modifier = Modifier
                                                                    .size(36.dp)
                                                                    .clip(CircleShape)
                                                                    .background(primaryPink.copy(alpha = 0.2f))
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Delete Category",
                                                                    tint = primaryPink,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } ?: Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = softPeach.copy(alpha = 0.8f)
                            )
                        ) {
                            Text(
                                "No budget available",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = primaryPink
                                )
                            )
                        }
                    }

                    // Expense Input Section with Enhanced Design
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (editingExpense == null) Icons.Default.Add else Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = primaryPink
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (editingExpense == null) "Add New Expense" else "Update Expense",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = primaryPink,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                // Category Dropdown with enhanced design
                                CategoryDropdown(
                                    label = "Category",
                                    options = budgetState?.categories?.keys?.toList() ?: emptyList(),
                                    selectedOption = selectedCategory,
                                    onOptionSelected = { selectedCategory = it },
                                    primaryColor = primaryPink,
                                    accentColor = accentPurple
                                )

                                // Amount Input with currency symbol
                                OutlinedTextField(
                                    value = amount,
                                    onValueChange = { amount = it },
                                    label = { Text("Expense Amount", color = primaryPink) },
                                    prefix = { Text("KSh ", color = primaryPink) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    colors = TextFieldDefaults.run {
                                        outlinedTextFieldColors(
                                            focusedBorderColor = accentPurple,
                                            unfocusedBorderColor = softPeach,
                                            cursorColor = primaryPink
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                // Description Input
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description", color = primaryPink) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    colors = TextFieldDefaults.run {
                                        outlinedTextFieldColors(
                                            focusedBorderColor = accentPurple,
                                            unfocusedBorderColor = softPeach,
                                            cursorColor = primaryPink
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )

                                // Save/Update Button with gradient
                                Button(
                                    onClick = {
                                        val expenseAmount = amount.toDoubleOrNull()
                                        if (expenseAmount == null || expenseAmount <= 0) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Enter a valid numeric amount greater than 0"
                                                )
                                            }
                                            return@Button
                                        }
                                        if (selectedCategory.isEmpty()) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Please select a category"
                                                )
                                            }
                                            return@Button
                                        }
                                        val categoryDetails = budgetState?.categories?.get(selectedCategory)
                                        val remainingBudget = (categoryDetails?.allocatedAmount ?: 0.0) -
                                                (categoryDetails?.spentAmount ?: 0.0) +
                                                (if (editingExpense?.category == selectedCategory) editingExpense?.amount ?: 0.0 else 0.0)
                                        if (expenseAmount > remainingBudget) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Expense exceeds remaining budget for $selectedCategory"
                                                )
                                            }
                                            return@Button
                                        }

                                        val expense = Expense(
                                            id = editingExpense?.id ?: "",
                                            category = selectedCategory,
                                            amount = expenseAmount,
                                            date = editingExpense?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                                            description = description
                                        )

                                        coroutineScope.launch {
                                            // If editing, delete the old expense first to adjust spent amount
                                            editingExpense?.let {
                                                repository.deleteExpense(
                                                    expenseId = it.id,
                                                    category = it.category,
                                                    amount = it.amount
                                                ).onFailure {
                                                    snackbarHostState.showSnackbar("Failed to update expense")
                                                    return@launch
                                                }
                                            }

                                            repository.saveExpense(expense).onSuccess {
                                                snackbarHostState.showSnackbar(
                                                    if (editingExpense != null) "Expense updated successfully"
                                                    else "Expense saved successfully"
                                                )
                                                // Clear form
                                                amount = ""
                                                description = ""
                                                selectedCategory = ""
                                                editingExpense = null
                                                // Show celebration animation
                                                showConfetti = true
                                            }.onFailure {
                                                snackbarHostState.showSnackbar("Failed to save expense")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentPurple,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (editingExpense == null) Icons.Default.Add else Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (editingExpense == null) "Save Expense" else "Update Expense",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Cancel Button (visible only when editing)
                                if (editingExpense != null) {
                                    Button(
                                        onClick = {
                                            // Clear form and reset editing state
                                            amount = ""
                                            description = ""
                                            selectedCategory = ""
                                            editingExpense = null
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryPink,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cancel", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Expenses List with Enhanced UI
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Recent Expenses",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Display expenses or placeholder
                    if (expensesState.isNotEmpty()) {
                        items(expensesState.sortedByDescending { it.date }) { expense ->
                            ExpenseItem(
                                expense = expense,
                                onEdit = {
                                    // Set fields for editing
                                    editingExpense = expense
                                    selectedCategory = expense.category
                                    amount = expense.amount.toString()
                                    description = expense.description
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        repository.deleteExpense(
                                            expenseId = expense.id,
                                            category = expense.category,
                                            amount = expense.amount
                                        ).onSuccess {
                                            snackbarHostState.showSnackbar("Expense deleted successfully")
                                        }.onFailure {
                                            snackbarHostState.showSnackbar("Failed to delete expense")
                                        }
                                    }
                                },
                                primaryColor = primaryPink,
                                accentColor = accentPurple
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = softPeach.copy(alpha = 0.8f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = primaryPink,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No expenses recorded yet",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = primaryPink
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Start tracking your expenses by adding them above",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.DarkGray
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Add padding at the bottom for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    )
}

@Composable
fun SparkleEffect(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkleTransition")
    val sparkleAlpha = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )

    Box(modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = primaryColor.copy(alpha = sparkleAlpha.value),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BudgetProgressIndicator(
    percentSpent: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val animatedPercent = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(percentSpent) {
        scope.launch {
            animatedPercent.animateTo(
                targetValue = min(percentSpent / 100f, 1f),
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .size(180.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = animatedPercent.value * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    0f to primaryColor,
                    0.5f to secondaryColor,
                    1f to primaryColor
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedPercent.value * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (animatedPercent.value > 0.9f) Color.Red else primaryColor
                )
            )
            Text(
                text = "Spent",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    primaryColor: Color,
    accentColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text(label, color = primaryColor) },
                trailingIcon = {
                    Icon(
                        Icons.Rounded.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = accentColor
                    )
                },
                colors = outlinedTextFieldColors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                    cursorColor = primaryColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clip(RoundedCornerShape(8.dp))
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                color = if (option == selectedOption) primaryColor else Color.Black
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.Black,
                            leadingIconColor = primaryColor
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    primaryColor: Color,
    accentColor: Color
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (expense.category) {
                                    "Food" -> Icons.Default.Restaurant
                                    "Shopping" -> Icons.Default.ShoppingCart
                                    "Transport" -> Icons.Default.DirectionsCar
                                    "Beauty" -> Icons.Default.Face
                                    "Entertainment" -> Icons.Default.MovieFilter
                                    else -> Icons.Default.LocalMall
                                },
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                expense.description.ifEmpty { expense.category },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    expense.category,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    "•",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    expense.date,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                // Amount and actions
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "KSh ${String.format("%.2f", expense.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Delete confirmation
            AnimatedVisibility(visible = showDeleteConfirm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }

                    TextButton(
                        onClick = {
                            onDelete()
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiAnimation(
    onAnimationFinish: () -> Unit
) {
    val confettiColors = listOf(
        Color(0xFFFf5f9e), // Pink
        Color(0xFFFFD700), // Gold
        Color(0xFFd8b4fe), // Purple
        Color(0xFFFfced3), // Peach
        Color(0xFFCDFADB)  // Mint
    )

    val infiniteTransition = rememberInfiniteTransition(label = "confettiTransition")
    val animatedProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onAnimationFinish()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Create 40 confetti pieces
        repeat(40) { i ->
            val color = confettiColors[i % confettiColors.size]
            val x = (i * 24) % canvasWidth
            val progress = (animatedProgress.value + (i * 0.02f)) % 1f
            val y = progress * canvasHeight

            // Draw the confetti piece
            val confettiSize = 12.dp.toPx()
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(confettiSize, confettiSize),
                alpha = 0.8f - progress
            )
        }
    }
}