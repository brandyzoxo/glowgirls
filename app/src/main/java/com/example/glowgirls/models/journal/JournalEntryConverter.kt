package com.example.glowgirls.models.journal

// Helper functions for Firebase
object JournalEntryConverter {
    fun toMap(entry: JournalEntry): Map<String, Any?> {
        return mapOf(
            "id" to entry.id,
            "userId" to entry.userId,
            "date" to entry.date,
            "emotion" to entry.emotion.name,
            "title" to entry.title,
            "content" to entry.content,
            "tags" to entry.tags,
            "isPrivate" to entry.isPrivate
        )
    }

    fun fromMap(map: Map<String, Any?>): JournalEntry {
        return JournalEntry(
            id = map["id"] as? String ?: "",
            userId = map["userId"] as? String ?: "",
            date = map["date"] as? Long ?: System.currentTimeMillis(),
            emotion = try {
                Emotion.valueOf(map["emotion"] as? String ?: "NEUTRAL")
            } catch (e: Exception) {
                Emotion.NEUTRAL
            },
            title = map["title"] as? String ?: "",
            content = map["content"] as? String ?: "",
            tags = (map["tags"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
            isPrivate = map["isPrivate"] as? Boolean != false
        )
    }
}