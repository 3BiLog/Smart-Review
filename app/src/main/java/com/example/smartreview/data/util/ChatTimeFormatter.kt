package com.example.smartreview.data.util

import java.util.Calendar

/**
 * Formats chat message timestamps from Firestore [createdAt] (epoch millis).
 * Uses device local timezone — no external date libraries.
 */
object ChatTimeFormatter {

    private const val JUST_NOW_THRESHOLD_MS = 60_000L

    /**
     * @param epochMillis Firestore createdAt (source of truth)
     * @param nowMillis reference clock, default [System.currentTimeMillis]
     */
    fun format(epochMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        if (epochMillis <= 0L) return ""

        val elapsed = nowMillis - epochMillis
        if (elapsed in 0 until JUST_NOW_THRESHOLD_MS) return "Vừa xong"

        val messageCal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }

        return when {
            isSameDay(messageCal, nowCal) -> formatSameDayTime(messageCal)
            isYesterday(messageCal, nowCal) -> "Hôm qua"
            isSameYear(messageCal, nowCal) -> formatDayMonth(messageCal)
            else -> formatDayMonthYear(messageCal)
        }
    }

    private fun formatSameDayTime(cal: Calendar): String {
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        return "%02d:%02d".format(hour, minute)
    }

    private fun formatDayMonth(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        return "%02d/%02d".format(day, month)
    }

    private fun formatDayMonthYear(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        return "%02d/%02d/%04d".format(day, month, year)
    }

    private fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(message: Calendar, now: Calendar): Boolean {
        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return message.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            message.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameYear(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR)

    /** Legacy mock / client strings that must not override [createdAt]. */
    fun isLegacyPlaceholderTime(value: String?): Boolean {
        if (value.isNullOrBlank()) return true
        return value.equals("Now", ignoreCase = true) ||
            value.equals("Vừa xong", ignoreCase = true)
    }
}
