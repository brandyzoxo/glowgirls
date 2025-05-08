package com.example.glowgirls.models.vision

data class Vision(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val imageUrl: String? = null,
    val progress: Int = 0, // 0 to 100
    val category: String = "General",
    val priority: Int = 0,
    val streak: Int = 0
)