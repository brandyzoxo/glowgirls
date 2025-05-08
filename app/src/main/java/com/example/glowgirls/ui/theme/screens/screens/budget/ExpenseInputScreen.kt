package com.example.glowgirls.ui.theme.screens.screens.budget

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    // Feminine color palette
    val primaryPink = Color(0xFFFf5f9e)
    val softPeach = Color(0xFFFfced3)
    val accentPurple = Color(0xFFd8b4fe)
    val gradient = Brush.verticalGradient(listOf(primaryPink, softPeach))

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient)
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Budget Summary Card
                item {
                    budgetState?.let { budget ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = softPeach.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
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
                                                        .clip(CircleShape)
                                                        .background(accentPurple.copy(alpha = 0.1f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Save,
                                                        contentDescription = "Save",
                                                        tint = accentPurple
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        editingCategory = null
                                                        newAllocatedAmount = ""
                                                    },
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(primaryPink.copy(alpha = 0.1f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Cancel",
                                                        tint = primaryPink
                                                    )
                                                }
                                            }
                                        } else {
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
                                                    Text(
                                                        "Allocated: KSh ${String.format("%.2f", details.allocatedAmount)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = Color.Black.copy(alpha = 0.8f)
                                                        )
                                                    )
                                                    Text(
                                                        "Spent: KSh ${String.format("%.2f", details.spentAmount)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = Color.Black.copy(alpha = 0.8f)
                                                        )
                                                    )
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
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            editingCategory = category
                                                            newAllocatedAmount = details.allocatedAmount.toString()
                                                        },
                                                        modifier = Modifier
                                                            .clip(CircleShape)
                                                            .background(accentPurple.copy(alpha = 0.1f))
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit Category",
                                                            tint = accentPurple
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
                                                            .clip(CircleShape)
                                                            .background(primaryPink.copy(alpha = 0.1f))
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Delete Category",
                                                            tint = primaryPink
                                                        )
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

                // Expense Input Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                if (editingExpense == null) "Add New Expense" else "Update Expense",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = primaryPink,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            // Category Dropdown
                            CategoryDropdown(
                                label = "Category",
                                options = budgetState?.categories?.keys?.toList() ?: emptyList(),
                                selectedOption = selectedCategory,
                                onOptionSelected = { selectedCategory = it },
                                primaryColor = primaryPink,
                                accentColor = accentPurple
                            )

                            // Amount Input
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                label = { Text("Expense Amount", color = primaryPink) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, accentPurple, RoundedCornerShape(8.dp)),
                                colors = TextFieldDefaults.run {
                                    outlinedTextFieldColors(
                                                                focusedBorderColor = accentPurple,
                                                                unfocusedBorderColor = softPeach
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
                                    .border(1.dp, accentPurple, RoundedCornerShape(8.dp)),
                                colors = TextFieldDefaults.run {
                                    outlinedTextFieldColors(
                                        focusedBorderColor = accentPurple,
                                        unfocusedBorderColor = softPeach
                                                            )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )

                            // Save/Update Button
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
                                        }.onFailure {
                                            snackbarHostState.showSnackbar("Failed to save expense")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentPurple,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    if (editingExpense == null) "Save Expense" else "Update Expense",
                                    fontWeight = FontWeight.Medium
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
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryPink,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Expenses List
                item {
                    Text(
                        "Recent Expenses",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = primaryPink,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(expensesState) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = softPeach.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${expense.category}: ${expense.description}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.Black.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    expense.date,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            }
                            Text(
                                "KSh ${String.format("%.2f", expense.amount)}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = primaryPink,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        // Populate form with expense data for editing
                                        amount = expense.amount.toString()
                                        description = expense.description
                                        selectedCategory = expense.category
                                        editingExpense = expense
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(accentPurple.copy(alpha = 0.1f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Expense",
                                        tint = accentPurple
                                    )
                                }
                                IconButton(
                                    onClick = {
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
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(primaryPink.copy(alpha = 0.1f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Expense",
                                        tint = primaryPink
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Section with Buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Share Budget Icon
                        IconButton(
                            onClick = {
                                budgetState?.let { budget ->
                                    // Construct a shareable budget summary
                                    val summary = buildString {
                                        append("Budget Overview\n")
                                        append("Period: ${budget.period}\n")
                                        append("Total Amount: KSh ${String.format("%.2f", budget.totalAmount)}\n")
                                        append("Start Date: ${budget.startDate}\n")
                                        append("\nCategories:\n")
                                        budget.categories.forEach { (category, details) ->
                                            append("- $category\n")
                                            append("  Allocated: KSh ${String.format("%.2f", details.allocatedAmount)}\n")
                                            append("  Spent: KSh ${String.format("%.2f", details.spentAmount)}\n")
                                            append("  Remaining: KSh ${String.format("%.2f", details.allocatedAmount - details.spentAmount)}\n")
                                        }
                                    }

                                    // Create share intent
                                    val shareIntent = ShareCompat.IntentBuilder(context)
                                        .setType("text/plain")
                                        .setSubject("My Budget Overview")
                                        .setText(summary)
                                        .intent

                                    // Start the share activity
                                    context.startActivity(shareIntent)
                                } ?: run {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("No budget available to share")
                                    }
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(accentPurple.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Budget",
                                tint = accentPurple
                            )
                        }

                        // View Spending Overview Button
                        Button(
                            onClick = { navController.navigate(ROUTE_SPENDING_SCREEN) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryPink,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "View Spending Overview",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    )
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

    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label, color = primaryColor) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
            readOnly = true,
            colors = TextFieldDefaults.run {
                outlinedTextFieldColors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.3f)
                    )
            },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = primaryColor
                    )
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(
                        option,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = primaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    ) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}