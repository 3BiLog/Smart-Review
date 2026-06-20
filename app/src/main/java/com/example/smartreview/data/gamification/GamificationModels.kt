package com.example.smartreview.data.gamification

enum class XpRewardAction(
    val xpAmount: Int,
    val countsTowardStreak: Boolean = true,
) {
    FLASHCARD_SESSION(xpAmount = 30),
    LESSON_COMPLETE(xpAmount = 50),
    QUIZ_COMPLETE(xpAmount = 40),
    DAILY_LOGIN(xpAmount = 10, countsTowardStreak = false),
}


sealed class GamificationRewardResult {
    data class Success(
        val xpAwarded: Int,
        val newXp: Int,
        val newStreak: Int,
        val streakIncremented: Boolean,
        val todayStudyKey: String,
    ) : GamificationRewardResult()

    data class AlreadyProcessed(
        val xpAwarded: Int = 0,
    ) : GamificationRewardResult()

    data object NotAuthenticated : GamificationRewardResult()

    data class Failed(val reason: String? = null) : GamificationRewardResult()
}