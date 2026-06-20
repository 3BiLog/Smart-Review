package com.example.smartreview.data.gamification

import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date

object StreakCalculator {

    fun computeStreak(
        currentStreak: Long,
        lastStreakDate: Timestamp?,
        today: Date = Date()
    ): StreakUpdateResult {
        val todayStart = getStartOfDay(today)
        val todayTimestamp = Timestamp(todayStart)

        if (lastStreakDate != null && isSameDay(lastStreakDate.toDate(), todayStart)) {
            return StreakUpdateResult(
                newStreak = currentStreak.coerceAtLeast(1),
                streakIncremented = false,
                todayTimestamp = todayTimestamp
            )
        }

        val newStreak = if (lastStreakDate != null && isYesterday(lastStreakDate.toDate(), todayStart)) {
            currentStreak.coerceAtLeast(1) + 1
        } else {
            1L
        }

        return StreakUpdateResult(
            newStreak = newStreak,
            streakIncremented = true,
            todayTimestamp = todayTimestamp
        )
    }

    @Deprecated("Use computeStreak with Timestamp instead", ReplaceWith("computeStreak(currentStreak.toLong(), lastStreakDate, today)"))
    fun computeStreakLegacy(
        currentStreak: Int,
        lastStudyDate: String?,
        todayKey: String = StudyDayFormatter.todayKey(),
    ): StreakUpdateLegacy {
        if (lastStudyDate == todayKey) {
            return StreakUpdateLegacy(
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
        return StreakUpdateLegacy(
            newStreak = newStreak,
            streakIncremented = true,
            todayStudyKey = todayKey,
        )
    }

    private fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        cal2.add(Calendar.DAY_OF_YEAR, -1)
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

data class StreakUpdateResult(
    val newStreak: Long,
    val streakIncremented: Boolean,
    val todayTimestamp: Timestamp
)

data class StreakUpdateLegacy(
    val newStreak: Int,
    val streakIncremented: Boolean,
    val todayStudyKey: String
)