package com.example.smartreview.data.learning

import com.example.smartreview.data.flashcard.FlashcardSessionStore
import com.example.smartreview.data.lesson.LessonSessionStore
import com.example.smartreview.data.quiz.QuizSessionStore

object LearningProgressCoordinator {

    fun clearInMemorySessionStores() {
        FlashcardSessionStore.clear()
        LessonSessionStore.clear()
        QuizSessionStore.clear()
    }
}
