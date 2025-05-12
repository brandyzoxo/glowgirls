package com.example.glowgirls.models.cycle


// Daily entry for tracking symptoms, mood and other metrics
data class DailyEntry(
    val id: String = "",
    val date: String = "",
    val cycleDay: Int? = null,
    val mood: String = "",
    val symptoms: List<String> = emptyList(),
    val notes: String = "",
    val flow: Flow? = null,
    val temperature: Float? = null,
    val sleep: Float? = null,  // Hours of sleep
    val energy: Int? = null,   // Energy level 1-10
    val exercise: Int? = null, // Minutes of exercise
    val stress: Int? = null,   // Stress level 1-10
    val metrics: Map<String, Float> = emptyMap() // Custom metrics
)

