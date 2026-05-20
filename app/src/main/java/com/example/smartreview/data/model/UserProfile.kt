package com.example.smartreview.data.model

/**
 * App-level user profile (Firestore users/{uid} + UI).
 */
data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String,
    val streak: Int = 0,
    val xp: Int = 0,
    val joinedAt: Long = 0L,
)
