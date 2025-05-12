package com.example.glowgirls.models.budget

data class SavingsGoal(
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val description: String = ""
)
