package com.example.glowgirls.models.budget

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Budget(
    val id: String = "",
    val period: String = "",
    val totalAmount: Double = 0.0,
    val startDate: String = "",
    val categories: Map<String, CategoryAllocation> = emptyMap(),
    val sharedWith: List<String> = emptyList(),
    val savingsGoal: SavingsGoal? = null
)

@IgnoreExtraProperties
data class CategoryAllocation(
    val allocatedAmount: Double = 0.0,
    val spentAmount: Double = 0.0
)

@IgnoreExtraProperties
data class Expense(
    val id: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: String = "", // Format: "yyyy-MM-dd"
    val description: String = ""
)

@IgnoreExtraProperties
data class CategoryProgress(
    val name: String = "",
    val allocated: Double = 0.0,
    val spent: Double = 0.0
)

