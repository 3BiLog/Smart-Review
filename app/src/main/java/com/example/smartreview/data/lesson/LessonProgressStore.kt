package com.example.smartreview.data.lesson

import com.example.smartreview.data.learning.LearningProgressServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lesson completion facade — persists per authenticated uid via [LearningProgressService].
 */
object LessonProgressStore {

    suspend fun markCompleted(lessonId: String) {
        withContext(Dispatchers.IO) {
            LearningProgressServiceProvider.default.markLessonCompleted(lessonId)
        }
    }

    suspend fun isCompleted(lessonId: String): Boolean = withContext(Dispatchers.IO) {
        LearningProgressServiceProvider.default.isLessonCompleted(lessonId)
    }
}