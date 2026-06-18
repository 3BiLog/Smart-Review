package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.UserProfile
import com.google.firebase.Timestamp

/**
 * Maps users/{uid} documents from the production Firestore schema to the app
 * domain model without exposing Firestore DTOs to UI.
 */
object UserFirestoreMapper {

    fun toUserProfile(documentId: String, data: Map<String, Any>?): UserProfile? {
        if (data == null) return null

        val dto = mapToUserDocument(data)
        val uid = documentId
        val email = dto.email.orEmpty()
        val displayName = dto.name?.takeIf { it.isNotBlank() } ?: defaultDisplayName(email, uid)

        return UserProfile(
            uid = uid,
            displayName = displayName,
            email = email,
            avatarUrl = defaultAvatarUrl(uid),
            phone = "",
            streak = dto.currentStreak ?: 0,
            xp = resolveTotalXp(dto),
            lastStreakDate = dto.lastStreakDate,
            joinedAt = dto.createdAt,
            longestStreak = dto.longestStreak ?: 0,
            role = dto.role ?: "user",
            status = dto.status ?: "active",
            warningCount = dto.warningCount ?: 0,
            lastLogin = dto.lastLogin,
            updatedAt = dto.updatedAt,
            bannedAt = dto.bannedAt,
            bannedUntil = dto.bannedUntil,
            bannedReason = dto.bannedReason,
            dailyGoal = dto.dailyGoal ?: 30,
            todayStudyTime = dto.todayStudyTime ?: 0,
            lastResetDate = dto.lastResetDate,
            dailyGoalXP = dto.dailyGoalXP ?: 0,
        )
    }

    /**
     * Update map for the signed-in user's own document.
     * [phone] is kept for UI contract compatibility; production schema has no phone field.
     */
    fun profileUpdateMap(displayName: String, phone: String): Map<String, Any> = mapOf(
        UserFirestorePaths.Fields.NAME to displayName.trim(),
        UserFirestorePaths.Fields.UPDATED_AT to Timestamp.now(),
    )

    // ✅ NEW: Update daily goal
    fun updateDailyGoalMap(dailyGoal: Long): Map<String, Any> = mapOf(
        UserFirestorePaths.Fields.DAILY_GOAL to dailyGoal,
        UserFirestorePaths.Fields.UPDATED_AT to Timestamp.now(),
    )

    // ✅ NEW: Reset today's study time
    fun resetTodayStudyTimeMap(): Map<String, Any> = mapOf(
        UserFirestorePaths.Fields.TODAY_STUDY_TIME to 0L,
        UserFirestorePaths.Fields.LAST_RESET_DATE to Timestamp.now(),
        UserFirestorePaths.Fields.DAILY_GOAL_XP to 0L,
        UserFirestorePaths.Fields.UPDATED_AT to Timestamp.now(),
    )

    // ✅ NEW: Add study time
    fun addStudyTimeMap(minutes: Long, xpEarned: Long): Map<String, Any> = mapOf(
        UserFirestorePaths.Fields.TODAY_STUDY_TIME to minutes,
        UserFirestorePaths.Fields.DAILY_GOAL_XP to xpEarned,
        UserFirestorePaths.Fields.UPDATED_AT to Timestamp.now(),
    )

    /**
     * Create a new user document in Firestore matching production schema.
     */
    fun newUserFirestoreMap(
        uid: String,
        email: String,
        displayName: String? = null,
    ): Map<String, Any> {
        val now = Timestamp.now()
        return mapOf(
            UserFirestorePaths.Fields.NAME to (
                displayName?.takeIf { it.isNotBlank() } ?: defaultDisplayName(email, uid)
                ),
            UserFirestorePaths.Fields.EMAIL to email.trim(),
            UserFirestorePaths.Fields.ROLE to "user",
            UserFirestorePaths.Fields.STATUS to "active",
            UserFirestorePaths.Fields.XP to 0L,
            UserFirestorePaths.Fields.TOTAL_XP to 0L,
            UserFirestorePaths.Fields.CURRENT_STREAK to 0L,
            UserFirestorePaths.Fields.LONGEST_STREAK to 0L,
            UserFirestorePaths.Fields.LAST_STREAK_DATE to now,
            UserFirestorePaths.Fields.CREATED_AT to now,
            UserFirestorePaths.Fields.UPDATED_AT to now,
            UserFirestorePaths.Fields.LAST_LOGIN to now,
            UserFirestorePaths.Fields.WARNING_COUNT to 0L,
            // ✅ NEW
            UserFirestorePaths.Fields.DAILY_GOAL to 30L,
            UserFirestorePaths.Fields.TODAY_STUDY_TIME to 0L,
            UserFirestorePaths.Fields.LAST_RESET_DATE to now,
            UserFirestorePaths.Fields.DAILY_GOAL_XP to 0L,
        )
    }

    fun defaultDisplayName(email: String, uid: String): String {
        val localPart = email.substringBefore("@").trim()
        if (localPart.isNotBlank()) return localPart.replaceFirstChar { it.uppercase() }
        return "SmartReview User"
    }

    fun defaultAvatarUrl(uid: String): String =
        "https://picsum.photos/seed/${uid.take(12)}/200/200"

    private fun resolveTotalXp(dto: UserDocument): Long =
        dto.totalXP ?: dto.xp ?: 0L

    private fun mapToUserDocument(data: Map<String, Any?>): UserDocument =
        UserDocument(
            name = stringField(data, UserFirestorePaths.Fields.NAME),
            email = stringField(data, UserFirestorePaths.Fields.EMAIL),
            role = stringField(data, UserFirestorePaths.Fields.ROLE),
            status = stringField(data, UserFirestorePaths.Fields.STATUS),
            xp = numberField(data, UserFirestorePaths.Fields.XP),
            totalXP = numberField(data, UserFirestorePaths.Fields.TOTAL_XP),
            currentStreak = numberField(data, UserFirestorePaths.Fields.CURRENT_STREAK),
            longestStreak = numberField(data, UserFirestorePaths.Fields.LONGEST_STREAK),
            lastStreakDate = timestampField(data, UserFirestorePaths.Fields.LAST_STREAK_DATE),
            createdAt = timestampField(data, UserFirestorePaths.Fields.CREATED_AT),
            updatedAt = timestampField(data, UserFirestorePaths.Fields.UPDATED_AT),
            lastLogin = timestampField(data, UserFirestorePaths.Fields.LAST_LOGIN),
            warningCount = numberField(data, UserFirestorePaths.Fields.WARNING_COUNT),
            bannedAt = timestampField(data, UserFirestorePaths.Fields.BANNED_AT),
            bannedUntil = timestampField(data, UserFirestorePaths.Fields.BANNED_UNTIL),
            bannedReason = stringField(data, UserFirestorePaths.Fields.BANNED_REASON),
            // ✅ NEW
            dailyGoal = numberField(data, UserFirestorePaths.Fields.DAILY_GOAL),
            todayStudyTime = numberField(data, UserFirestorePaths.Fields.TODAY_STUDY_TIME),
            lastResetDate = timestampField(data, UserFirestorePaths.Fields.LAST_RESET_DATE),
            dailyGoalXP = numberField(data, UserFirestorePaths.Fields.DAILY_GOAL_XP),
        )

    private fun stringField(data: Map<String, Any?>, vararg keys: String): String? {
        for (key in keys) {
            val value = data[key] as? String
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun numberField(data: Map<String, Any?>, vararg keys: String): Long? {
        for (key in keys) {
            when (val value = data[key]) {
                is Number -> return value.toLong()
                is String -> return value.toLongOrNull()
            }
        }
        return null
    }

    private fun timestampField(data: Map<String, Any?>, vararg keys: String): Timestamp? {
        for (key in keys) {
            when (val value = data[key]) {
                is Timestamp -> return value
            }
        }
        return null
    }
}
