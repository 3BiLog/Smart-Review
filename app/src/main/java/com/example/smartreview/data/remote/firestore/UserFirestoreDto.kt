package com.example.smartreview.data.remote.firestore

import com.google.firebase.Timestamp

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
    val dailyGoal: Long? = null,
    val todayStudyTime: Long? = null,
    val lastResetDate: Timestamp? = null,
    val dailyGoalXP: Long? = null,
)
