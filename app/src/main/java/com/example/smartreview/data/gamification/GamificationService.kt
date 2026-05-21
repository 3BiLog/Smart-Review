package com.example.smartreview.data.gamification

import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import com.example.smartreview.data.repository.GamificationRepository
import com.example.smartreview.data.repository.GamificationRepositoryProvider

/**
 * Central entry point for XP rewards and streak progression across features.
 */
class GamificationService(
    private val gamificationRepository: GamificationRepository = GamificationRepositoryProvider.default,
    private val authRepository: AuthRepository = AuthRepositoryProvider.default,
) {

    suspend fun reward(
        action: XpRewardAction,
        idempotencyKey: String,
    ): GamificationRewardResult {
        val uid = authRepository.getCurrentUser()?.uid
            ?: return GamificationRewardResult.NotAuthenticated
        if (idempotencyKey.isBlank()) return GamificationRewardResult.Failed
        return gamificationRepository.applyReward(uid, action, idempotencyKey)
    }

    suspend fun rewardFlashcardSession(sessionId: String): GamificationRewardResult =
        reward(
            action = XpRewardAction.FLASHCARD_SESSION,
            idempotencyKey = "flashcard_session_$sessionId",
        )

    suspend fun rewardLessonComplete(lessonId: String): GamificationRewardResult =
        reward(
            action = XpRewardAction.LESSON_COMPLETE,
            idempotencyKey = "lesson_complete_$lessonId",
        )

    suspend fun rewardQuizComplete(quizId: String): GamificationRewardResult =
        reward(
            action = XpRewardAction.QUIZ_COMPLETE,
            idempotencyKey = "quiz_complete_$quizId",
        )

    suspend fun rewardDailyLogin(): GamificationRewardResult =
        reward(
            action = XpRewardAction.DAILY_LOGIN,
            idempotencyKey = "daily_login_${StudyDayFormatter.todayKey()}",
        )
}
