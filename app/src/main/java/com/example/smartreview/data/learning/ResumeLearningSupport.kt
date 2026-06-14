package com.example.smartreview.data.learning

import android.util.Log
import com.example.smartreview.data.model.FlashcardProgressSnapshot
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.model.QuizProgressSnapshot
import com.example.smartreview.data.model.UserLearningProgress
import com.example.smartreview.data.repository.FlashcardRepositoryProvider
import com.example.smartreview.data.repository.LessonRepositoryProvider
import com.example.smartreview.data.repository.QuizRepositoryProvider
import kotlinx.coroutines.runBlocking

/**
 * Validates in-progress snapshots and drops stale entries (completed / invalid).
 */
object ResumeLearningSupport {

    const val LOG_TAG = "ResumeLearning"

    fun logLoadedSnapshot(progress: UserLearningProgress) {
        Log.d(
            LOG_TAG,
            "loaded snapshot: flashcard=${progress.flashcardInProgress?.deckId}, " +
                    "lesson=${progress.lessonInProgress?.lessonId}, " +
                    "quiz=${progress.quizInProgress?.quizId}, " +
                    "completedLessons=${progress.completedLessonIds.size}, " +
                    "completedQuizzes=${progress.completedQuizIds.size}",
        )
    }

    fun sanitize(progress: UserLearningProgress): Pair<UserLearningProgress, List<String>> {
        val filteredReasons = mutableListOf<String>()
        var sanitized = progress

        progress.flashcardInProgress?.let { snap ->
            if (!isFlashcardSnapshotResumable(snap)) {
                filteredReasons += "flashcard:${snap.deckId}"
                sanitized = sanitized.copy(flashcardInProgress = null)
            }
        }

        progress.lessonInProgress?.let { snap ->
            if (!isLessonSnapshotResumable(snap, progress)) {
                filteredReasons += "lesson:${snap.lessonId}"
                sanitized = sanitized.copy(lessonInProgress = null)
            }
        }

        progress.quizInProgress?.let { snap ->
            if (!isQuizSnapshotResumable(snap, progress)) {
                filteredReasons += "quiz:${snap.quizId}"
                sanitized = sanitized.copy(quizInProgress = null)
            }
        }

        return sanitized to filteredReasons
    }

    // FIXED: Use runBlocking for Flashcard repository
    fun isFlashcardSnapshotResumable(snapshot: FlashcardProgressSnapshot): Boolean {
        if (snapshot.deckId.isBlank()) return false
        val deck = runBlocking { FlashcardRepositoryProvider.default.getDeck(snapshot.deckId) } ?: return false
        val total = deck.cards.size
        if (total == 0) return false
        val studied = snapshot.knownCount + snapshot.reviewCount
        return studied < total
    }

    fun isLessonSnapshotResumable(
        snapshot: LessonProgressSnapshot,
        progress: UserLearningProgress,
    ): Boolean {
        if (snapshot.lessonId.isBlank()) return false
        if (snapshot.lessonId in progress.completedLessonIds) return false
        val lesson = LessonRepositoryProvider.default.getLesson(snapshot.lessonId) ?: return false
        val policy = LearningProgressionPolicy()
        val policySnapshot = LearningProgressionPolicy.ProgressSnapshot(
            completedLessonIds = progress.completedLessonIds,
            completedQuizIds = progress.completedQuizIds,
        )
        if (policy.isLessonFullyComplete(snapshot.lessonId, policySnapshot)) return false
        return true
    }

    fun isQuizSnapshotResumable(
        snapshot: QuizProgressSnapshot,
        progress: UserLearningProgress,
    ): Boolean {
        if (snapshot.quizId.isBlank()) return false
        if (snapshot.quizId in progress.completedQuizIds) return false
        val quiz = runBlocking { QuizRepositoryProvider.default.getQuiz(snapshot.quizId) } ?: return false
        val lessonId = quiz.lessonId?.takeIf { it.isNotBlank() }
        if (lessonId != null) {
            if (lessonId in progress.completedLessonIds) return false
            val policy = LearningProgressionPolicy()
            val policySnapshot = LearningProgressionPolicy.ProgressSnapshot(
                completedLessonIds = progress.completedLessonIds,
                completedQuizIds = progress.completedQuizIds,
            )
            if (policy.isLessonFullyComplete(lessonId, policySnapshot)) return false
        }
        val total = quiz.questions.size
        if (total == 0) return false
        return snapshot.answers.size < total
    }
}