package com.example.smartreview.data.flashcard

import com.example.smartreview.data.model.FlashcardSessionResult
import java.util.concurrent.ConcurrentHashMap

object FlashcardSessionStore {

    private val sessions = ConcurrentHashMap<String, FlashcardSessionResult>()

    fun put(result: FlashcardSessionResult) {
        sessions[result.sessionId] = result
    }

    fun consume(sessionId: String): FlashcardSessionResult? =
        sessions.remove(sessionId)

    fun clear() = sessions.clear()
}
