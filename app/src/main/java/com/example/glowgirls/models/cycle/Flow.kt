package com.example.glowgirls.models.cycle

// Flow intensity enum
enum class Flow(val displayName: String, val description: String) {
    LIGHT("Light", "Lighter than usual flow"),
    MEDIUM("Medium", "Normal flow"),
    HEAVY("Heavy", "Heavier than usual flow"),
    SPOTTING("Spotting", "Very light bleeding or spotting")
}
