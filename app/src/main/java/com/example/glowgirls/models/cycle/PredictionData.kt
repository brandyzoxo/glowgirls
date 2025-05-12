package com.example.glowgirls.models.cycle

data class PredictionData(
    val predictedNextPeriod: String = "",
    val predictedCycleLength: Int = 0,
    val predictedPeriodDuration: Int = 0,
    val certainty: Float = 0f,  // 0-1 value of prediction certainty
    val symptomPredictions: Map<String, Float> = emptyMap() // Predicted symptoms and likelihood
)
