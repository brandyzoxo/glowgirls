package com.example.glowgirls.models.journal

// Journal Entry data class
data class JournalEntry(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val emotion: Emotion = Emotion.NEUTRAL,
    val title: String = "",
    val content: String = "",
    val tags: List<String> = listOf(),
    val isPrivate: Boolean = true
)