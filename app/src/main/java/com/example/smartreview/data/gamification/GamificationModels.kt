package com.example.smartreview.data.gamification

/**
 * Canonical XP reward actions for lessons, quizzes, flashcards, and future features.
 */
enum class XpRewardAction(
    val xpAmount: Int,
    val countsTowardStreak: Boolean = true,
) {
    FLASHCARD_SESSION(xpAmount = 30),
    LESSON_COMPLETE(xpAmount = 50),
    QUIZ_COMPLETE(xpAmount = 40),
    DAILY_LOGIN(xpAmount = 10, countsTowardStreak = false),
}

// REMOVED: StreakUpdate - now using StreakUpdateResult from StreakCalculator.kt

sealed class GamificationRewardResult {
    data class Success(
        val xpAwarded: Int,
        val newXp: Int,
        val newStreak: Int,
        val streakIncremented: Boolean,
        val todayStudyKey: String,
    ) : GamificationRewardResult()

    /** Idempotency key already processed — no duplicate XP. */
    data class AlreadyProcessed(
        val xpAwarded: Int = 0,
    ) : GamificationRewardResult()

    data object NotAuthenticated : GamificationRewardResult()

    /** Firestore reward failed; [reason] is for logging/UI (never treat as success). */
    data class Failed(val reason: String? = null) : GamificationRewardResult()
}