package com.example.smartreview.data.gamification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakCalculatorTest {

    @Test
    fun computeStreak_sameDay_doesNotIncrement() {
        val today = "2026-05-20"
        val update = StreakCalculator.computeStreak(
            currentStreak = 5,
            lastStudyDate = today,
            todayKey = today,
        )
        assertEquals(5, update.newStreak)
        assertFalse(update.streakIncremented)
    }

    @Test
    fun computeStreak_consecutiveDay_increments() {
        val update = StreakCalculator.computeStreak(
            currentStreak = 3,
            lastStudyDate = "2026-05-19",
            todayKey = "2026-05-20",
        )
        assertEquals(4, update.newStreak)
        assertTrue(update.streakIncremented)
    }

    @Test
    fun computeStreak_gapResetsToOne() {
        val update = StreakCalculator.computeStreak(
            currentStreak = 10,
            lastStudyDate = "2026-05-10",
            todayKey = "2026-05-20",
        )
        assertEquals(1, update.newStreak)
        assertTrue(update.streakIncremented)
    }

    @Test
    fun computeStreak_firstStudyDay_startsAtOne() {
        val update = StreakCalculator.computeStreak(
            currentStreak = 0,
            lastStudyDate = null,
            todayKey = "2026-05-20",
        )
        assertEquals(1, update.newStreak)
        assertTrue(update.streakIncremented)
    }
}
