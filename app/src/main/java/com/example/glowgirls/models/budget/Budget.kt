package com.example.glowgirls.models.budget

data class Budget(
    val id: String = "",
    val period: String = "",
    val totalAmount: Double = 0.0,
    val startDate: String = "",
    val categories: Map<String, CategoryAllocation> = emptyMap(),
    val sharedWith: List<String> = emptyList(),
    val savingsGoal: SavingsGoal? = null
)

data class CategoryAllocation(
    val allocatedAmount: Double = 0.0,
    val spentAmount: Double = 0.0
)

data class Expense(
    val id: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val description: String = ""
)

data class CategoryProgress(
    val name: String,
    val allocated: Double,
    val spent: Double
)

