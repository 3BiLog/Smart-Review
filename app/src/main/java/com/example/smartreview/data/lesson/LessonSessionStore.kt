package com.example.smartreview.data.lesson

import com.example.smartreview.data.model.LessonCompletionResult
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory handoff between Lesson and LessonSummary screens.
 */
object LessonSessionStore {

    private val sessions = ConcurrentHashMap<String, LessonCompletionResult>()

    fun put(result: LessonCompletionResult) {
        sessions[result.sessionId] = result
    }

    fun consume(sessionId: String): LessonCompletionResult? = sessions.remove(sessionId)

    fun clear() = sessions.clear()
}
