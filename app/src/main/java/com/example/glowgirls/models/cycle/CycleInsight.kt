package com.example.glowgirls.models.cycle

data class CycleInsight(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: InsightCategory = InsightCategory.GENERAL,
    val relevancyScore: Float = 0f,  // How relevant this insight is to the user
    val recommendations: List<String> = emptyList()
)
