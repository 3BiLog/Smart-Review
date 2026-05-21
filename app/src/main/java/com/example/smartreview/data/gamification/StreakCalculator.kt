package com.example.smartreview.data.gamification

/**
 * Pure streak rules based on [lastStudyDate] (yyyy-MM-dd) and today's local key.
 */
object StreakCalculator {

    fun computeStreak(
        currentStreak: Int,
        lastStudyDate: String?,
        todayKey: String = StudyDayFormatter.todayKey(),
    ): StreakUpdate {
        if (lastStudyDate == todayKey) {
            return StreakUpdate(
                newStreak = currentStreak.coerceAtLeast(1),
                streakIncremented = false,
                todayStudyKey = todayKey,
            )
        }
        val gapDays = StudyDayFormatter.daysBetween(lastStudyDate, todayKey)
        val newStreak = when {
            lastStudyDate.isNullOrBlank() -> 1
            gapDays == 1L -> (currentStreak.coerceAtLeast(1) + 1)
            else -> 1
        }
        return StreakUpdate(
            newStreak = newStreak,
            streakIncremented = true,
            todayStudyKey = todayKey,
        )
    }
}
