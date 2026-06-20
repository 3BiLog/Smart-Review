package com.example.smartreview.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName


data class UserProfile(
    @get:PropertyName("uid")
    val uid: String = "",

    @get:PropertyName("name")
    val displayName: String = "",

    val email: String = "",

    @get:PropertyName("photoURL")
    val avatarUrl: String? = null,

    val phone: String = "",

    @get:PropertyName("currentStreak")
    val streak: Long = 0,

    @get:PropertyName("totalXP")
    val xp: Long = 0,

    @get:PropertyName("xp")
    val xpLegacy: Long = 0,

    @get:PropertyName("lastStreakDate")
    val lastStreakDate: Timestamp? = null,

    @get:PropertyName("createdAt")
    val joinedAt: Timestamp? = null,

    @get:PropertyName("longestStreak")
    val longestStreak: Long = 0,

    @get:PropertyName("role")
    val role: String = "user",

    @get:PropertyName("status")
    val status: String = "active",

    @get:PropertyName("warningCount")
    val warningCount: Long = 0,

    @get:PropertyName("lastLogin")
    val lastLogin: Timestamp? = null,

    @get:PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,

    @get:PropertyName("bannedAt")
    val bannedAt: Timestamp? = null,

    @get:PropertyName("bannedUntil")
    val bannedUntil: Timestamp? = null,

    @get:PropertyName("bannedReason")
    val bannedReason: String? = null,

    @get:PropertyName("dailyGoal")
    val dailyGoal: Long = 30,

    @get:PropertyName("todayStudyTime")
    val todayStudyTime: Long = 0,

    @get:PropertyName("lastResetDate")
    val lastResetDate: Timestamp? = null,

    @get:PropertyName("dailyGoalXP")
    val dailyGoalXP: Long = 0,
) {
    fun isBanned(): Boolean {
        if (status != "active") return true
        val bannedUntilDate = bannedUntil?.toDate() ?: return false
        return bannedUntilDate.after(java.util.Date())
    }

    fun resolvedDisplayName(): String = displayName.ifEmpty { email.split("@").first() }

    fun isDailyGoalCompleted(): Boolean {
        return todayStudyTime >= dailyGoal
    }

    fun getDailyGoalProgress(): Float {
        return if (dailyGoal > 0) todayStudyTime.toFloat() / dailyGoal.toFloat() else 0f
    }
}