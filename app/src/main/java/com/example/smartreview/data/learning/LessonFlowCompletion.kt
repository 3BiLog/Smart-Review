package com.example.smartreview.data.learning

import com.example.smartreview.data.lesson.LessonProgressStore
import com.example.smartreview.data.lesson.LessonSessionStore
import com.example.smartreview.data.model.LessonCompletionResult
import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizCompletionResult
import com.example.smartreview.data.repository.GamificationServiceProvider
import com.example.smartreview.data.repository.LessonRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

/**
 * Completes a course-linked quiz and prepares lesson completion summary data.
 */
object LessonFlowCompletion {

    data class LessonSummaryHandoff(
        val lessonSummarySessionId: String,
        val courseId: String,
    )

    fun completeQuizAndPrepareLessonSummary(
        scope: CoroutineScope,
        quiz: Quiz,
        quizResult: QuizCompletionResult,
        onReady: (LessonSummaryHandoff?) -> Unit,
    ) {
        val lessonId = quiz.lessonId?.takeIf { it.isNotBlank() } ?: run {
            onReady(null)
            return
        }
        val lesson = runBlocking { LessonRepositoryProvider.default.getLesson(lessonId) } ?: run {
            onReady(null)
            return
        }
        val contentBlocks = LessonContentBlocks.contentBlocks(lesson)
        val lessonSessionId = UUID.randomUUID().toString()
        val lessonCompletion = LessonCompletionResult(
            sessionId = lessonSessionId,
            lessonId = lesson.id,
            courseId = lesson.courseId,
            lessonTitle = lesson.title,
            totalBlocks = contentBlocks.size.coerceAtLeast(1),
            viewedBlocks = contentBlocks.size.coerceAtLeast(1),
            durationMs = quizResult.durationMs,
        )
        LessonSessionStore.put(lessonCompletion)

        scope.launch {
            val progress = LearningProgressServiceProvider.default
            progress.markQuizCompleted(quiz.id)
            GamificationServiceProvider.default.rewardQuizComplete(quiz.id)
            LessonProgressStore.markCompleted(lesson.id)
            onReady(LessonSummaryHandoff(lessonSessionId, lesson.courseId))
        }
    }
}