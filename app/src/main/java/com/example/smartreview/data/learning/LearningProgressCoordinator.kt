package com.example.smartreview.data.learning

import com.example.smartreview.data.flashcard.FlashcardSessionStore
import com.example.smartreview.data.lesson.LessonSessionStore
import com.example.smartreview.data.quiz.QuizSessionStore

/**
 * Clears ephemeral in-memory session handoffs when auth session ends.
 * Persisted per-uid progress remains in SharedPreferences.
 */
object LearningProgressCoordinator {

    fun clearInMemorySessionStores() {
        FlashcardSessionStore.clear()
        LessonSessionStore.clear()
        QuizSessionStore.clear()
    }
}
