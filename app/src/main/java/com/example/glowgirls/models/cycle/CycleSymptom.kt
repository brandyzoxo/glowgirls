package com.example.glowgirls.models.cycle

// Common symptoms enum for selection
enum class CycleSymptom(val category: String, val displayName: String) {
    // Physical Symptoms
    CRAMPS("Physical", "Cramps"),
    HEADACHE("Physical", "Headache"),
    BACKACHE("Physical", "Backache"),
    FATIGUE("Physical", "Fatigue"),
    BLOATING("Physical", "Bloating"),
    BREAST_TENDERNESS("Physical", "Breast Tenderness"),
    ACNE("Physical", "Acne"),
    INSOMNIA("Physical", "Insomnia"),

    // Emotional Symptoms
    MOOD_SWINGS("Emotional", "Mood Swings"),
    IRRITABILITY("Emotional", "Irritability"),
    ANXIETY("Emotional", "Anxiety"),
    DEPRESSION("Emotional", "Depression"),
    LOW_ENERGY("Emotional", "Low Energy"),

    // Others
    CRAVINGS("Other", "Food Cravings"),
    DIGESTIVE_ISSUES("Other", "Digestive Issues"),
    NAUSEA("Other", "Nausea"),
    DIZZINESS("Other", "Dizziness")
}
