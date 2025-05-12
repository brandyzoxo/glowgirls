package com.example.glowgirls.models.chat

// Updated ChatMessage model with chatRoomId

data class ChatMessage(
    val messageId: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val profilePictureUrl: String = "",
    val mood: MessageMood = MessageMood.NEUTRAL
// Added field for profile picture
)

