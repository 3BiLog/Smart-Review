package com.example.smartreview.data.remote.firestore

object UserFirestorePaths {
    const val USERS = "users"

    object Fields {
        const val NAME = "name"
        const val EMAIL = "email"
        const val ROLE = "role"
        const val STATUS = "status"
        const val XP = "xp"
        const val TOTAL_XP = "totalXP"
        const val CURRENT_STREAK = "currentStreak"
        const val LONGEST_STREAK = "longestStreak"
        const val LAST_STREAK_DATE = "lastStreakDate"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val LAST_LOGIN = "lastLogin"
        const val WARNING_COUNT = "warningCount"
        const val BANNED_AT = "bannedAt"
        const val BANNED_UNTIL = "bannedUntil"
        const val BANNED_REASON = "bannedReason"
        const val DAILY_GOAL = "dailyGoal"
        const val TODAY_STUDY_TIME = "todayStudyTime"
        const val LAST_RESET_DATE = "lastResetDate"
        const val DAILY_GOAL_XP = "dailyGoalXP"
    }
}
