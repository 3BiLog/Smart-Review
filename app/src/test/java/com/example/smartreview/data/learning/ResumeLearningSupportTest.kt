package com.example.smartreview.data.learning

import com.example.smartreview.data.content.ContentIds
import com.example.smartreview.data.model.FlashcardProgressSnapshot
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.model.QuizProgressSnapshot
import com.example.smartreview.data.model.UserLearningProgress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumeLearningSupportTest {

    @Test
    fun lessonSnapshot_notResumable_whenLessonCompleted() {
        val progress = UserLearningProgress(
            uid = "u1",
            completedLessonIds = setOf(ContentIds.Lesson.REACT_INTRO),
            lessonInProgress = LessonProgressSnapshot(
                lessonId = ContentIds.Lesson.REACT_INTRO,
                sessionId = "s1",
                sessionStartedAt = 1L,
                currentBlockIndex = 0,
                viewedBlockIds = emptySet(),
            ),
        )
        val snap = progress.lessonInProgress!!
        assertFalse(ResumeLearningSupport.isLessonSnapshotResumable(snap, progress))
    }

    @Test
    fun lessonSnapshot_notResumable_whenInvalidLessonId() {
        val progress = UserLearningProgress(
            uid = "u1",
            lessonInProgress = LessonProgressSnapshot(
                lessonId = "lesson_does_not_exist",
                sessionId = "s1",
                sessionStartedAt = 1L,
                currentBlockIndex = 0,
                viewedBlockIds = emptySet(),
            ),
        )
        val snap = progress.lessonInProgress!!
        assertFalse(ResumeLearningSupport.isLessonSnapshotResumable(snap, progress))
    }

    @Test
    fun sanitize_clearsCompletedLessonSnapshot() {
        val progress = UserLearningProgress(
            uid = "u1",
            completedLessonIds = setOf(ContentIds.Lesson.REACT_INTRO),
            lessonInProgress = LessonProgressSnapshot(
                lessonId = ContentIds.Lesson.REACT_INTRO,
                sessionId = "s1",
                sessionStartedAt = 1L,
                currentBlockIndex = 0,
                viewedBlockIds = emptySet(),
            ),
        )
        val (sanitized, reasons) = ResumeLearningSupport.sanitize(progress)
        assertNull(sanitized.lessonInProgress)
        assertTrue(reasons.any { it.startsWith("lesson:") })
    }

    @Test
    fun quizSnapshot_notResumable_whenQuizCompleted() {
        val progress = UserLearningProgress(
            uid = "u1",
            completedQuizIds = setOf(ContentIds.Quiz.REACT_INTRO),
            quizInProgress = QuizProgressSnapshot(
                quizId = ContentIds.Quiz.REACT_INTRO,
                sessionId = "s1",
                sessionStartedAt = 1L,
                currentIndex = 0,
                answers = emptyList(),
            ),
        )
        val snap = progress.quizInProgress!!
        assertFalse(ResumeLearningSupport.isQuizSnapshotResumable(snap, progress))
    }

    @Test
    fun flashcardSnapshot_notResumable_whenDeckMissing() {
        val snap = FlashcardProgressSnapshot(
            deckId = "unknown_deck_xyz",
            sessionId = "s1",
            sessionStartedAt = 1L,
            currentIndex = 0,
            cardStatuses = emptyMap(),
        )
        assertFalse(ResumeLearningSupport.isFlashcardSnapshotResumable(snap))
    }
}
