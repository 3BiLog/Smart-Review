package com.example.smartreview.data.quiz

import com.example.smartreview.data.model.QuizCompletionResult
import java.util.concurrent.ConcurrentHashMap

object QuizSessionStore {

    private val sessions = ConcurrentHashMap<String, QuizCompletionResult>()

    fun put(result: QuizCompletionResult) {
        sessions[result.sessionId] = result
    }

    fun consume(sessionId: String): QuizCompletionResult? = sessions.remove(sessionId)

    fun clear() = sessions.clear()
}
