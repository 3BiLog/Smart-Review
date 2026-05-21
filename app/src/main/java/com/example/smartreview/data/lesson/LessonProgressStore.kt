package com.example.smartreview.data.lesson

import com.example.smartreview.data.learning.LearningProgressServiceProvider
import kotlinx.coroutines.runBlocking

/**
 * Lesson completion facade — persists per authenticated uid via [LearningProgressService].
 */
object LessonProgressStore {

    fun markCompleted(lessonId: String) = runBlocking {
        LearningProgressServiceProvider.default.markLessonCompleted(lessonId)
    }

    fun isCompleted(lessonId: String): Boolean = runBlocking {
        LearningProgressServiceProvider.default.isLessonCompleted(lessonId)
    }
}
