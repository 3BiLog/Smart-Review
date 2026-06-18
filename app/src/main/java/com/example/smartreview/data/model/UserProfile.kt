package com.example.smartreview.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * User profile that matches Firestore schema from Web Admin (DA3-master).
 *
 * Firestore collection: users/{uid}
 *
 * Field mappings:
 * - displayName (App) -> name (Firestore)
 * - streak (App) -> currentStreak (Firestore)
 * - xp (App) -> totalXP (Firestore)
 * - lastStudyDate (App) -> lastStreakDate (Firestore)
 * - joinedAt (App) -> createdAt (Firestore)
 */
data class UserProfile(
    @get:PropertyName("uid")
    val uid: String = "",

    // FIXED: "displayName" -> "name" to match Web Admin schema
    @get:PropertyName("name")
    val displayName: String = "",

    val email: String = "",

    // FIXED: "avatarUrl" -> "photoURL" to match Web Admin schema
    @get:PropertyName("photoURL")
    val avatarUrl: String? = null,

    val phone: String = "",

    // FIXED: "streak" -> "currentStreak"
    @get:PropertyName("currentStreak")
    val streak: Long = 0,

    // FIXED: "xp" -> "totalXP" (leaderboard uses this)
    @get:PropertyName("totalXP")
    val xp: Long = 0,

    // Keep legacy xp field for backward compatibility
    @get:PropertyName("xp")
    val xpLegacy: Long = 0,

    // FIXED: String -> Timestamp, "lastStudyDate" -> "lastStreakDate"
    @get:PropertyName("lastStreakDate")
    val lastStreakDate: Timestamp? = null,

    // FIXED: Long -> Timestamp, "joinedAt" -> "createdAt"
    @get:PropertyName("createdAt")
    val joinedAt: Timestamp? = null,

    // NEW fields from Web Admin schema
    @get:PropertyName("longestStreak")
    val longestStreak: Long = 0,

    @get:PropertyName("role")
    val role: String = "user",  // "user" or "admin"

    @get:PropertyName("status")
    val status: String = "active",  // "active", "banned", "inactive"

    @get:PropertyName("warningCount")
    val warningCount: Long = 0,

    @get:PropertyName("lastLogin")
    val lastLogin: Timestamp? = null,

    @get:PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,

    // Ban fields (if user is banned)
    @get:PropertyName("bannedAt")
    val bannedAt: Timestamp? = null,

    @get:PropertyName("bannedUntil")
    val bannedUntil: Timestamp? = null,

    @get:PropertyName("bannedReason")
    val bannedReason: String? = null,

    // ✅ NEW: Daily goal in minutes (15, 30, 60)
    @get:PropertyName("dailyGoal")
    val dailyGoal: Long = 30,

    // ✅ NEW: Today's study time in minutes
    @get:PropertyName("todayStudyTime")
    val todayStudyTime: Long = 0,

    // ✅ NEW: Date of last study time reset (to track daily)
    @get:PropertyName("lastResetDate")
    val lastResetDate: Timestamp? = null,

    // ✅ NEW: XP earned from completing daily goal
    @get:PropertyName("dailyGoalXP")
    val dailyGoalXP: Long = 0,
) {
    // Helper function to check if user is banned
    fun isBanned(): Boolean {
        if (status != "active") return true
        val bannedUntilDate = bannedUntil?.toDate() ?: return false
        return bannedUntilDate.after(java.util.Date())
    }

    // Helper to get display name with fallback (not named getDisplayName — JVM clashes with property getter)
    fun resolvedDisplayName(): String = displayName.ifEmpty { email.split("@").first() }

    // ✅ Helper: Check if daily goal is completed today
    fun isDailyGoalCompleted(): Boolean {
        return todayStudyTime >= dailyGoal
    }

    // ✅ Helper: Get study time progress as float (0.0 - 1.0)
    fun getDailyGoalProgress(): Float {
        return if (dailyGoal > 0) todayStudyTime.toFloat() / dailyGoal.toFloat() else 0f
    }
}