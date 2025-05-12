package com.example.glowgirls.models.profile


data class UserProfile(
    val username: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val posts: Int = 0,
    val streakDays: Int = 0,
    val achievements: Int = 0,

    // Made all fields non-nullable with defaults
    val cycleDays: Int = 0,
    val glowPoints: Int = 0,
    val budgetMaster: Boolean = false,
    val visionSet: Boolean = false,

    // Added timestamp fields for better tracking
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis(),

    // Optional - you can add a privacy setting if needed
    val isProfilePrivate: Boolean = false
)