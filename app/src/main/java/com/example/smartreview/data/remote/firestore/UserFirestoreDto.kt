package com.example.smartreview.data.remote.firestore

import com.google.firebase.Timestamp

/**
 * Firestore document shape for users/{uid}.
 *
 * Field names match the production Firestore schema exactly.
 * Document ID is the Firebase UID and is not duplicated in this DTO.
 */
data class UserDocument(
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val status: String? = null,
    val xp: Long? = null,
    val totalXP: Long? = null,
    val currentStreak: Long? = null,
    val longestStreak: Long? = null,
    val lastStreakDate: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val warningCount: Long? = null,
    val bannedAt: Timestamp? = null,
    val bannedUntil: Timestamp? = null,
    val bannedReason: String? = null,
)
