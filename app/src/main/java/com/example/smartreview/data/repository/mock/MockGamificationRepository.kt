package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.gamification.StreakCalculator
import com.example.smartreview.data.gamification.StudyDayFormatter
import com.example.smartreview.data.gamification.XpRewardAction
import com.example.smartreview.data.repository.GamificationRepository

/**
 * In-memory idempotent rewards for offline/debug; syncs [MockUserRepository] profile state.
 */
class MockGamificationRepository(
    private val mockUserRepository: MockUserRepository = MockUserRepository(),
) : GamificationRepository {

    private val processedKeys = mutableSetOf<String>()

    override suspend fun applyReward(
        uid: String,
        action: XpRewardAction,
        idempotencyKey: String,
    ): GamificationRewardResult {
        if (idempotencyKey.isBlank()) return GamificationRewardResult.Failed
        synchronized(processedKeys) {
            if (idempotencyKey in processedKeys) {
                return GamificationRewardResult.AlreadyProcessed()
            }
            processedKeys.add(idempotencyKey)
        }

        val profile = mockUserRepository.getCurrentUserProfile()
        if (profile.uid != uid) return GamificationRewardResult.Failed

        val todayKey = StudyDayFormatter.todayKey()
        val streakUpdate = if (action.countsTowardStreak) {
            StreakCalculator.computeStreak(profile.streak, profile.lastStudyDate, todayKey)
        } else {
            null
        }

        val newXp = profile.xp + action.xpAmount
        val newStreak = streakUpdate?.newStreak ?: profile.streak
        val newLastStudy = streakUpdate?.todayStudyKey ?: profile.lastStudyDate

        mockUserRepository.applyGamificationUpdate(
            newXp = newXp,
            newStreak = newStreak,
            lastStudyDate = newLastStudy,
        )

        return GamificationRewardResult.Success(
            xpAwarded = action.xpAmount,
            newXp = newXp,
            newStreak = newStreak,
            streakIncremented = streakUpdate?.streakIncremented ?: false,
            todayStudyKey = newLastStudy,
        )
    }
}
