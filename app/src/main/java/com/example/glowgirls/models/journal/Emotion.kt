package com.example.glowgirls.models.journal

import androidx.compose.ui.graphics.Color

//Emotion enum with color associations for UI
enum class Emotion(val displayName: String, val color: Color) {
    JOY("Joy", Color(0xFFFFC107)),
    GRATITUDE("Gratitude", Color(0xFF8BC34A)),
    SERENITY("Serenity", Color(0xFF03A9F4)),
    LOVE("Love", Color(0xFFE91E63)),
    CONFIDENCE("Confidence", Color(0xFF673AB7)),
    INSPIRED("Inspired", Color(0xFF009688)),
    ANXIOUS("Anxious", Color(0xFFFF5722)),
    SAD("Sad", Color(0xFF2196F3)),
    ANGRY("Angry", Color(0xFFF44336)),
    TIRED("Tired", Color(0xFF795548)),
    STRESSED("Stressed", Color(0xFF9C27B0)),
    NEUTRAL("Neutral", Color(0xFF9E9E9E))
}