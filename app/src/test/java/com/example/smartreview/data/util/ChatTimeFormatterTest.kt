package com.example.smartreview.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ChatTimeFormatterTest {

    @Test
    fun format_sameDay_returnsHourMinute() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 20, 14, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val epoch = cal.timeInMillis
        assertEquals("14:30", ChatTimeFormatter.format(epoch, nowMillis = epoch + 120_000L))
    }

    @Test
    fun format_withinOneMinute_returnsVuaXong() {
        val now = System.currentTimeMillis()
        assertEquals("Vừa xong", ChatTimeFormatter.format(now - 30_000L, nowMillis = now))
    }

    @Test
    fun format_yesterday_returnsHomQua() {
        val now = Calendar.getInstance()
        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val epoch = yesterday.timeInMillis
        assertEquals("Hôm qua", ChatTimeFormatter.format(epoch, nowMillis = now.timeInMillis))
    }

    @Test
    fun isLegacyPlaceholderTime_detectsNow() {
        assertTrue(ChatTimeFormatter.isLegacyPlaceholderTime("Now"))
        assertTrue(ChatTimeFormatter.isLegacyPlaceholderTime("Vừa xong"))
    }
}
