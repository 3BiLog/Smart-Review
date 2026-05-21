package com.example.smartreview.data.flashcard

import com.example.smartreview.data.model.FlashcardSessionResult
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory session handoff between Flashcard and Summary screens (local-first).
 * Replaced later by SavedStateHandle + Room or Firestore session docs.
 */
object FlashcardSessionStore {

    private val sessions = ConcurrentHashMap<String, FlashcardSessionResult>()

    fun put(result: FlashcardSessionResult) {
        sessions[result.sessionId] = result
    }

    /** Returns session once and removes it to avoid stale replays. */
    fun consume(sessionId: String): FlashcardSessionResult? =
        sessions.remove(sessionId)

    fun clear() = sessions.clear()
}
