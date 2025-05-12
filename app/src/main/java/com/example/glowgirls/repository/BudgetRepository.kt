package com.example.glowgirls.repository

import com.example.glowgirls.models.budget.Budget
import com.example.glowgirls.models.budget.CategoryProgress
import com.example.glowgirls.models.budget.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class BudgetRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveBudget(budget: Budget): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val budgetId = if (budget.id.isNotEmpty()) {
                budget.id // Use existing ID for updates
            } else {
                database.child("users").child(userId).child("budgets").push().key
                    ?: throw Exception("Failed to generate budget ID")
            }
            database.child("users").child(userId).child("budgets").child(budgetId)
                .setValue(budget.copy(id = budgetId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveExpense(expense: Expense): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val expenseId = if (expense.id.isNotEmpty()) {
                expense.id // Use existing ID for updates
            } else {
                database.child("users").child(userId).child("expenses").push().key
                    ?: throw Exception("Failed to generate expense ID")
            }
            database.child("users").child(userId).child("expenses").child(expenseId)
                .setValue(expense.copy(id = expenseId)).await()
            updateSpentAmount(expense.category, expense.amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateSpentAmount(category: String, amount: Double) {
        val userId = auth.currentUser?.uid ?: return
        val snapshot = database.child("users").child(userId).child("budgets")
            .limitToLast(1).get().await()

        snapshot.children.forEach { budgetSnapshot ->
            val budgetId = budgetSnapshot.key ?: return@forEach
            val currentSpent = budgetSnapshot.child("categories").child(category)
                .child("spentAmount").getValue(Double::class.java) ?: 0.0

            database.child("users").child(userId).child("budgets")
                .child(budgetId).child("categories").child(category)
                .child("spentAmount").setValue(currentSpent + amount).await()
        }
    }

    /**
     * Updates a category's allocation amount in the latest budget
     */
    suspend fun updateCategoryAllocation(categoryName: String, newAllocation: Double): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = database.child("users").child(userId).child("budgets")
                .limitToLast(1).get().await()

            val budgetSnapshot = snapshot.children.firstOrNull()
                ?: throw Exception("No budget found")

            val budgetId = budgetSnapshot.key!!

            database.child("users").child(userId).child("budgets")
                .child(budgetId).child("categories").child(categoryName)
                .child("allocatedAmount").setValue(newAllocation).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLatestBudget(): Flow<Budget?> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val budget = snapshot.children.firstOrNull()?.let {
                    it.getValue(Budget::class.java)?.copy(id = it.key!!)
                }
                trySend(budget)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.child("users").child(userId).child("budgets").limitToLast(1)
            .addValueEventListener(listener)
        awaitClose {
            database.child("users").child(userId).child("budgets").removeEventListener(listener)
        }
    }

    suspend fun getBudgetById(budgetId: String): Result<Budget> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = database.child("users").child(userId).child("budgets").child(budgetId)
                .get().await()
            val budget = snapshot.getValue(Budget::class.java)?.copy(id = budgetId)
                ?: throw Exception("Budget not found")
            Result.success(budget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBudget(budgetId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            database.child("users").child(userId).child("budgets").child(budgetId)
                .removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expenseId: String, category: String, amount: Double): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            // Delete the expense
            database.child("users").child(userId).child("expenses").child(expenseId)
                .removeValue().await()
            // Update the spent amount by subtracting the deleted expense amount
            val snapshot = database.child("users").child(userId).child("budgets")
                .limitToLast(1).get().await()
            snapshot.children.forEach { budgetSnapshot ->
                val budgetId = budgetSnapshot.key!!
                val currentSpent = budgetSnapshot.child("categories").child(category)
                    .child("spentAmount").getValue(Double::class.java) ?: 0.0
                val newSpent = max(0.0, currentSpent - amount) // Prevent negative spent amounts
                database.child("users").child(userId).child("budgets")
                    .child(budgetId).child("categories").child(category)
                    .child("spentAmount").setValue(newSpent).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // This simpler version can be implemented if you want to hide the category/amount details
    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // First get the expense details
            val expenseSnapshot = database.child("users").child(userId).child("expenses")
                .child(expenseId).get().await()

            val expense = expenseSnapshot.getValue(Expense::class.java)
                ?: throw Exception("Expense not found")

            // Then delete using the full method
            deleteExpense(expenseId, expense.category, expense.amount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategoryFromBudget(budgetId: String, categoryName: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = database.child("users").child(userId).child("budgets").child(budgetId)
                .get().await()
            val budget = snapshot.getValue(Budget::class.java)?.copy(id = budgetId)
                ?: throw Exception("Budget not found")
            val updatedCategories = budget.categories.filter { it.key != categoryName }.toMap()
            database.child("users").child(userId).child("budgets").child(budgetId)
                .child("categories").setValue(updatedCategories).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCategoryProgress(): Flow<List<CategoryProgress>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val progressList = mutableListOf<CategoryProgress>()
                snapshot.children.firstOrNull()
                    ?.child("categories")?.children?.forEach { categorySnapshot ->
                        val category = categorySnapshot.key!!
                        val allocated =
                            categorySnapshot.child("allocatedAmount").getValue(Double::class.java)
                                ?: 0.0
                        val spent =
                            categorySnapshot.child("spentAmount").getValue(Double::class.java)
                                ?: 0.0
                        progressList.add(CategoryProgress(category, allocated, spent))
                    }
                trySend(progressList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.child("users").child(userId).child("budgets").limitToLast(1)
            .addValueEventListener(listener)
        awaitClose {
            database.child("users").child(userId).child("budgets").removeEventListener(listener)
        }
    }

    fun getExpenses(): Flow<List<Expense>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val expenses = snapshot.children.mapNotNull { expenseSnapshot ->
                    expenseSnapshot.getValue(Expense::class.java)?.copy(id = expenseSnapshot.key!!)
                }
                trySend(expenses)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.child("users").child(userId).child("expenses")
            .addValueEventListener(listener)
        awaitClose {
            database.child("users").child(userId).child("expenses").removeEventListener(listener)
        }
    }

    fun getBudgetSuggestions(): Flow<Map<String, Double>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        var expensesListener: ValueEventListener? = null
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val suggestions = mutableMapOf<String, Double>()
                expensesListener = object : ValueEventListener {
                    override fun onDataChange(expenseSnapshot: DataSnapshot) {
                        val categoryTotals = mutableMapOf<String, Double>()
                        expenseSnapshot.children.forEach { expense ->
                            val exp = expense.getValue(Expense::class.java) ?: return@forEach
                            categoryTotals[exp.category] =
                                (categoryTotals[exp.category] ?: 0.0) + exp.amount
                        }

                        snapshot.children.firstOrNull()
                            ?.child("categories")?.children?.forEach { categorySnapshot ->
                                val name = categorySnapshot.key!!
                                val allocated = categorySnapshot.child("allocatedAmount")
                                    .getValue(Double::class.java) ?: 0.0
                                val spent = categoryTotals[name] ?: 0.0
                                if (spent > allocated) {
                                    suggestions[name] =
                                        spent * 1.1 // Suggest 10% more than current spending
                                }
                            }
                        trySend(suggestions)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        close(error.toException())
                    }
                }
                database.child("users").child(userId).child("expenses")
                    .addValueEventListener(expensesListener!!)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        database.child("users").child(userId).child("budgets").limitToLast(1)
            .addValueEventListener(listener)
        awaitClose {
            database.child("users").child(userId).child("budgets").removeEventListener(listener)
            expensesListener?.let { listener ->
                database.child("users").child(userId).child("expenses")
                    .removeEventListener(listener)
            }
        }
    }

    /**
     * Calculates a budget health score based on several financial metrics
     * @param budget The current budget object (may be null if no budget exists)
     * @param expenses List of expense transactions
     * @return An integer score from 0 to 100 representing budget health
     */
    fun calculateBudgetHealthScore(budget: Budget?, expenses: List<Expense>): Int {
        // Return a baseline score if no budget exists
        if (budget == null) return 50

        // Calculate total budget and spending
        val totalBudgeted = budget.categories.values.sumOf { it.allocatedAmount }
        val totalSpent = budget.categories.values.sumOf { it.spentAmount }

        // If no budget allocated, return minimum score
        if (totalBudgeted <= 0) return 40

        // Calculate various health metrics

        // 1. Overall budget utilization (30 points)
        val utilizationRatio = totalSpent / totalBudgeted
        val utilizationScore = when {
            utilizationRatio <= 0.85 -> 30 // Under budget
            utilizationRatio <= 1.0 -> 25  // Slightly under budget
            utilizationRatio <= 1.1 -> 15  // Slightly over budget
            utilizationRatio <= 1.25 -> 10 // Moderately over budget
            else -> 5                      // Significantly over budget
        }

        // 2. Category balance (30 points)
        // Check if any individual categories are severely over budget
        var categoryBalanceScore = 30
        var overBudgetCategories = 0
        var severelyOverBudgetCategories = 0

        budget.categories.forEach { (_, details) ->
            if (details.allocatedAmount > 0) {
                val categoryRatio = details.spentAmount / details.allocatedAmount
                when {
                    categoryRatio > 1.5 -> severelyOverBudgetCategories++
                    categoryRatio > 1.0 -> overBudgetCategories++
                }
            }
        }

        // Deduct points based on over-budget categories
        if (budget.categories.isNotEmpty()) {
            // Calculate proportional deductions
            val totalCategories = budget.categories.size
            categoryBalanceScore -= (overBudgetCategories * 5.0 / totalCategories).toInt()
            categoryBalanceScore -= (severelyOverBudgetCategories * 10.0 / totalCategories).toInt()
            categoryBalanceScore = categoryBalanceScore.coerceAtLeast(5)
        }

        // 3. Spending consistency (20 points)
        // Group expenses by date to analyze spending patterns
        val expensesByDate = expenses
            .filter { it.date.isNotBlank() }
            .groupBy { it.date.split("-").take(2).joinToString("-") } // Group by year-month
            .mapValues { it.value.sumOf { expense -> expense.amount } }

        val consistencyScore = if (expensesByDate.size >= 2) {
            val values = expensesByDate.values.toList()
            val avg = values.average()
            val variance = values.sumOf { (it - avg).pow(2) } / values.size
            val stdDev = sqrt(variance)
            val coeffVar = if (avg > 0) stdDev / avg else 1.0

            when {
                coeffVar < 0.2 -> 20  // Very consistent spending
                coeffVar < 0.4 -> 15  // Moderately consistent
                coeffVar < 0.6 -> 10  // Somewhat inconsistent
                else -> 5             // Very inconsistent
            }
        } else {
            10 // Not enough data for consistency analysis
        }

        // 4. Expense distribution (20 points)
        // Check if spending is concentrated in a few categories
        val distributionScore = if (budget.categories.isNotEmpty()) {
            val categorySpending = budget.categories.values.map { it.spentAmount }
            val totalSpending = categorySpending.sum()

            if (totalSpending > 0) {
                // Calculate Gini coefficient (measure of distribution inequality)
                val sortedSpending = categorySpending.sorted()
                var sumOfDifferences = 0.0
                for (i in sortedSpending.indices) {
                    for (j in sortedSpending.indices) {
                        sumOfDifferences += abs(sortedSpending[i] - sortedSpending[j])
                    }
                }

                val gini = sumOfDifferences / (2 * categorySpending.size * categorySpending.size * totalSpending / categorySpending.size)

                when {
                    gini < 0.3 -> 20  // Very balanced distribution
                    gini < 0.5 -> 15  // Moderately balanced
                    gini < 0.7 -> 10  // Somewhat imbalanced
                    else -> 5         // Very imbalanced (concentrated in few categories)
                }
            } else {
                10 // No spending recorded
            }
        } else {
            10 // No categories defined
        }

        // Calculate final score (sum of all component scores)
        val finalScore = utilizationScore + categoryBalanceScore + consistencyScore + distributionScore

        // Return score capped at 100
        return finalScore.coerceAtMost(100)
    }

    /**
     * Generates personalized financial insights based on budget and expense data
     * @param budget The current budget object (may be null if no budget exists)
     * @param expenses List of expense transactions
     * @return List of insight messages
     */
    fun generateFinancialInsights(budget: Budget?, expenses: List<Expense>): List<String> {
        val insights = mutableListOf<String>()

        if (budget == null) {
            insights.add("Set up a budget to start tracking your financial health")
            return insights
        }

        // Calculate total budget and spending
        val totalBudgeted = budget.categories.values.sumOf { it.allocatedAmount }
        val totalSpent = budget.categories.values.sumOf { it.spentAmount }

        // Overall budget status
        val remainingBudget = totalBudgeted - totalSpent
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val daysRemaining = daysInMonth - currentDay + 1

        if (remainingBudget <= 0) {
            insights.add("You've exceeded your monthly budget. Consider reviewing your spending in high-expense categories.")
        } else {
            val dailyBudget = remainingBudget / daysRemaining
            insights.add("You have KSh ${String.format("%.2f", remainingBudget)} remaining for ${daysRemaining} days (KSh ${String.format("%.2f", dailyBudget)}/day).")
        }

        // Category-specific insights
        val overBudgetCategories = budget.categories.filter {
            it.value.allocatedAmount > 0 && it.value.spentAmount > it.value.allocatedAmount
        }

        if (overBudgetCategories.isNotEmpty()) {
            val worstCategory = overBudgetCategories.maxByOrNull {
                it.value.spentAmount - it.value.allocatedAmount
            }

            worstCategory?.let {
                val overspentAmount = it.value.spentAmount - it.value.allocatedAmount
                insights.add("Your ${it.key} category is over budget by KSh ${String.format("%.2f", overspentAmount)}. Try to limit spending in this area.")
            }
        }

        // Find nearly depleted categories (over 85% used)
        val nearlyDepletedCategories = budget.categories.filter {
            it.value.allocatedAmount > 0 &&
                    it.value.spentAmount / it.value.allocatedAmount > 0.85 &&
                    it.value.spentAmount < it.value.allocatedAmount
        }

        if (nearlyDepletedCategories.isNotEmpty()) {
            val criticalCategory = nearlyDepletedCategories.maxByOrNull {
                it.value.spentAmount / it.value.allocatedAmount
            }

            criticalCategory?.let {
                val remainingAmount = it.value.allocatedAmount - it.value.spentAmount
                val usedPercentage = (it.value.spentAmount / it.value.allocatedAmount * 100).toInt()
                insights.add("Your ${it.key} budget is ${usedPercentage}% used with KSh ${String.format("%.2f", remainingAmount)} remaining. Plan carefully.")
            }
        }

        // Find untouched or barely used categories (under 10% used)
        val underutilizedCategories = budget.categories.filter {
            it.value.allocatedAmount > 0 &&
                    it.value.spentAmount / it.value.allocatedAmount < 0.1
        }

        if (underutilizedCategories.isNotEmpty() && currentDay > 15) {
            val mostUnderutilized = underutilizedCategories.minByOrNull {
                it.value.spentAmount / it.value.allocatedAmount
            }

            mostUnderutilized?.let {
                insights.add("Your ${it.key} budget is hardly used. Consider reallocating funds if you don't plan to use it.")
            }
        }

        // Spending patterns
        if (expenses.size >= 5) {
            val recentExpenses = expenses.sortedByDescending { it.date }.take(5)
            val recentTotal = recentExpenses.sumOf { it.amount }
            val recentAvg = recentTotal / recentExpenses.size

            if (recentAvg > (totalBudgeted / daysInMonth) * 2) {
                insights.add("Your recent spending average (KSh ${String.format("%.2f", recentAvg)}) is higher than your daily budget. Consider slowing down.")
            }

            // Detect frequent small expenses
            val frequentSmallExpenses = expenses
                .filter { it.amount < totalBudgeted * 0.01 }
                .groupBy { it.description }
                .filter { it.value.size >= 3 }

            if (frequentSmallExpenses.isNotEmpty()) {
                val mostFrequent = frequentSmallExpenses.maxByOrNull { it.value.size }
                mostFrequent?.let {
                    val total = it.value.sumOf { expense -> expense.amount }
                    insights.add("Small, frequent purchases for \"${it.key}\" add up to KSh ${String.format("%.2f", total)}. These small expenses can impact your budget.")
                }
            }
        }

        // Return the insights, limiting to 5 to avoid overwhelming the user
        return insights.take(5)
    }
}