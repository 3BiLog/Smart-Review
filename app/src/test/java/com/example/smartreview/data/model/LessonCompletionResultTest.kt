package com.example.smartreview.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LessonCompletionResultTest {

    @Test
    fun progress_and_timeFormat() {
        val result = LessonCompletionResult(
            sessionId = "s1",
            lessonId = "l1",
            courseId = "c1",
            lessonTitle = "Intro",
            totalBlocks = 4,
            viewedBlocks = 3,
            durationMs = 90_000,
        )
        assertEquals(0.75f, result.progress, 0.001f)
        assertEquals("01:30", result.formattedStudyTime())
    }
}
