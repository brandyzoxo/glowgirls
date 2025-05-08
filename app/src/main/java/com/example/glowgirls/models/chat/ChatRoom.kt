package com.example.glowgirls.models.chat

// New ChatRoom model
data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = 0,
    val memberCount: Int = 0,
    val imageUrl: String = "" // Optional: for chat room icon/image
)