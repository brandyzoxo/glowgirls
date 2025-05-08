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
import kotlinx.coroutines.tasks.await
import kotlin.math.max

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
            val expenseId = database.child("users").child(userId).child("expenses").push().key
                ?: throw Exception("Failed to generate expense ID")
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
            val budgetId = budgetSnapshot.key!!
            val currentSpent = budgetSnapshot.child("categories").child(category)
                .child("spentAmount").getValue(Double::class.java) ?: 0.0
            database.child("users").child(userId).child("budgets")
                .child(budgetId).child("categories").child(category)
                .child("spentAmount").setValue(currentSpent + amount).await()
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
}