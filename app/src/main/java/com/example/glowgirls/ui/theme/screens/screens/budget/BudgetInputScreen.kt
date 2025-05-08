package com.example.glowgirls.ui.theme.screens.screens.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glowgirls.repository.BudgetRepository
import com.example.glowgirls.models.budget.Budget
import com.example.glowgirls.models.budget.CategoryAllocation
import com.example.glowgirls.models.budget.SavingsGoal
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

    val categories = remember { mutableStateListOf<CategoryAllocationWrapper>() }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Automatically add savings as a category if target is set
    val savingsTargetValue = savingsTarget.toDoubleOrNull() ?: 0.0
    if (savingsTargetValue > 0 && !categories.any { it.name.equals("Savings", ignoreCase = true) }) {
        categories.add(CategoryAllocationWrapper("Savings", savingsTargetValue))
    } else if (savingsTargetValue <= 0 && categories.any { it.name.equals("Savings", ignoreCase = true) }) {
        categories.removeIf { it.name.equals("Savings", ignoreCase = true) }
    }

    // Calculate remaining balance
    val totalAmountValue = totalAmount.toDoubleOrNull() ?: 0.0
    val allocatedAmount = categories.sumOf { it.amount }
    val remainingBalance = totalAmountValue - allocatedAmount

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightPink,
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    item {
                        Text(
                            "Create Your Budget",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = DeepRose,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Plan your finances with style",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.Gray
                            )
                        )
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

                                // Period Dropdown
                                PeriodDropdownField(
                                    selectedOption = period,
                                    onOptionSelected = { period = it }
                                )

                                // Total Budget Input
                                OutlinedTextField(
                                    value = totalAmount,
                                    onValueChange = { totalAmount = it },
                                    label = { Text("Total Budget Amount") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.AttachMoney,
                                            contentDescription = null,
                                            tint = DeepRose
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DeepRose,
                                        focusedLabelColor = DeepRose,
                                        focusedLeadingIconColor = DeepRose,
                                    ),
                                    shape = RoundedCornerShape(12.dp)
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
                            }
                        }
                    }

                    // Balance Display Card
                    item {
                        val isPositiveBalance = remainingBalance >= 0
                        val balanceColor = if (isPositiveBalance) MintGreen else RosePink
                        val textColor = if (isPositiveBalance) Color(0xFF2E7D32) else Color(0xFFD32F2F)

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
                            }
                        }
                    }

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
                                    horizontalArrangement = Arrangement.SpaceBetween,
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

                                if (categories.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No categories added yet.\nTap + to add your first category.",
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    categories.forEach { category ->
                                        CategoryAllocationItem(
                                            category = category,
                                            onAmountChanged = { amount ->
                                                categories[categories.indexOf(category)] = category.copy(amount = amount)
                                            },
                                            onDelete = {
                                                categories.remove(category)
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
                                    startDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
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
                                    repository.saveBudget(budget).onSuccess {
                                        snackbarHostState.showSnackbar("Budget saved successfully")
                                        onBudgetSaved()
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("Failed to save budget")
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
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
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
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCategoryAdded: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

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
fun PeriodDropdownField(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Weekly", "Monthly")

    Column {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text("Budget Period") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepRose,
                focusedLabelColor = DeepRose
            ),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Show Options",
                        tint = DeepRose
                    )
                }
            }
        )

        DropdownMenu(
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
                            color = if (option == selectedOption) DeepRose else Color.Black
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    leadingIcon = {
                        if (option == selectedOption) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(DeepRose, CircleShape)
                            )
                        }
                    }
                )
            }
        }
    }
}

data class CategoryAllocationWrapper(
    val name: String,
    var amount: Double = 0.0
)

@Composable
fun CategoryAllocationItem(
    category: CategoryAllocationWrapper,
    onAmountChanged: (Double) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = SoftBlue.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF37474F)
                )
            }

            OutlinedTextField(
                value = if (category.amount > 0) category.amount.toString() else "",
                onValueChange = { onAmountChanged(it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Amount", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.width(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepRose,
                    focusedLabelColor = DeepRose
                )
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Category",
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}