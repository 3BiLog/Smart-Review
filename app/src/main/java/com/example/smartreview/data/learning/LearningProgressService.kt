package com.example.smartreview.data.learning

import android.util.Log
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.CardStudyStatus
import com.example.smartreview.data.model.FlashcardProgressSnapshot
import com.example.smartreview.data.model.LearningProgressionItem
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.model.QuizProgressSnapshot
import com.example.smartreview.data.model.UserLearningProgress
import com.example.smartreview.data.repository.LearningProgressRepository
import com.example.smartreview.data.repository.LearningProgressRepositoryProvider

/**
 * Authenticated learning progress API (persist + resume). Gamification remains separate.
 */
class LearningProgressService(
    private val repository: LearningProgressRepository = LearningProgressRepositoryProvider.default,
    private val progressionResolver: LearningProgressionResolver = LearningProgressionResolver(),
) {

    suspend fun currentProgress(): UserLearningProgress? {
        val uid = AuthSession.currentUserId() ?: return null
        return repository.load(uid) ?: UserLearningProgress(uid = uid)
    }

    private suspend fun update(transform: (UserLearningProgress) -> UserLearningProgress) {
        val uid = AuthSession.currentUserId() ?: return
        val current = repository.load(uid) ?: UserLearningProgress(uid = uid)
        val updated = transform(current).copy(uid = uid, lastUpdatedAt = System.currentTimeMillis())
        if (updated.uid != uid) return
        repository.save(updated)
    }

    suspend fun isLessonCompleted(lessonId: String): Boolean {
        val progress = currentProgress() ?: return false
        return lessonId in progress.completedLessonIds
    }

    suspend fun isQuizCompleted(quizId: String): Boolean {
        val progress = currentProgress() ?: return false
        return quizId in progress.completedQuizIds
    }

    suspend fun markLessonCompleted(lessonId: String) {
        update { progress ->
            progress.copy(
                completedLessonIds = progress.completedLessonIds + lessonId,
                lessonInProgress = null,
            )
        }
    }

    suspend fun markQuizCompleted(quizId: String) {
        update { progress ->
            progress.copy(
                completedQuizIds = progress.completedQuizIds + quizId,
                quizInProgress = null,
            )
        }
    }

    suspend fun saveFlashcardSnapshot(snapshot: FlashcardProgressSnapshot) {
        update { it.copy(flashcardInProgress = snapshot) }
    }

    suspend fun clearFlashcardInProgress() {
        update { it.copy(flashcardInProgress = null) }
    }

    suspend fun flashcardSnapshotForDeck(deckId: String): FlashcardProgressSnapshot? =
        currentProgress()?.flashcardInProgress?.takeIf { it.deckId == deckId }

    suspend fun saveLessonSnapshot(snapshot: LessonProgressSnapshot) {
        update { it.copy(lessonInProgress = snapshot) }
    }

    suspend fun clearLessonInProgress() {
        update { it.copy(lessonInProgress = null) }
    }

    suspend fun lessonSnapshotForLesson(lessonId: String): LessonProgressSnapshot? =
        currentProgress()?.lessonInProgress?.takeIf { it.lessonId == lessonId }

    suspend fun saveQuizSnapshot(snapshot: QuizProgressSnapshot) {
        update {
            it.copy(
                quizInProgress = snapshot,
                lessonInProgress = null,
            )
        }
    }

    suspend fun clearQuizInProgress() {
        update { it.copy(quizInProgress = null) }
    }

    suspend fun quizSnapshotForQuiz(quizId: String): QuizProgressSnapshot? =
        currentProgress()?.quizInProgress?.takeIf { it.quizId == quizId }

    suspend fun resumeLearningItems(): List<LearningProgressionItem> {
        val uid = AuthSession.currentUserId()
        if (uid == null) {
            Log.d(ResumeLearningSupport.LOG_TAG, "empty state: not authenticated")
            return emptyList()
        }

        val progress = currentProgress() ?: UserLearningProgress(uid = uid)
        ResumeLearningSupport.logLoadedSnapshot(progress)

        val (sanitized, filteredReasons) = ResumeLearningSupport.sanitize(progress)
        filteredReasons.forEach { reason ->
            Log.d(ResumeLearningSupport.LOG_TAG, "filtered completed: $reason")
        }
        if (sanitized != progress) {
            repository.save(sanitized)
        }

        val items = progressionResolver.resolveFromProgress(sanitized)
        if (items.isEmpty()) {
            Log.d(ResumeLearningSupport.LOG_TAG, "empty state")
        } else {
            items.forEach { item ->
                Log.d(
                    ResumeLearningSupport.LOG_TAG,
                    "resume item: type=${item.type} id=${item.contentId} route=${item.route}",
                )
            }
        }
        if (filteredReasons.isNotEmpty() && items.isEmpty()) {
            Log.d(ResumeLearningSupport.LOG_TAG, "empty state after filtering stale snapshots")
        }
        return items
    }

    fun cardStatusFromSnapshot(map: Map<String, String>): Map<String, CardStudyStatus> =
        map.mapValues { (_, raw) ->
            runCatching { CardStudyStatus.valueOf(raw) }.getOrDefault(CardStudyStatus.UNSEEN)
        }

    fun cardStatusToSnapshot(map: Map<String, CardStudyStatus>): Map<String, String> =
        map.mapValues { (_, status) -> status.name }
}

object LearningProgressServiceProvider {
    val default: LearningProgressService = LearningProgressService()
}
