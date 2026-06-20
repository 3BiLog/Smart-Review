package com.example.smartreview.data.gamification

import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object StudyDayFormatter {

    fun todayKey(
        timeMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
    ): String = dayKeyFromMillis(timeMillis, timeZone)

    fun dayKeyFromMillis(timeMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val cal = Calendar.getInstance(timeZone)
        cal.timeInMillis = timeMillis
        return formatCal(cal)
    }

    fun daysBetween(fromKey: String?, toKey: String): Long? {
        if (fromKey.isNullOrBlank()) return null
        val from = parseDayKey(fromKey) ?: return null
        val to = parseDayKey(toKey) ?: return null
        val diffMs = to.timeInMillis - from.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diffMs)
    }

    private fun formatCal(cal: Calendar): String =
        String.format(
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )

    private fun parseDayKey(key: String): Calendar? = runCatching {
        val parts = key.split("-")
        if (parts.size != 3) return null
        Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }.getOrNull()
}
