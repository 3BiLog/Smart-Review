package com.example.smartreview.data.learning

import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.mock.MockFlashcardData
import com.example.smartreview.data.model.CardStudyStatus
import com.example.smartreview.data.model.FlashcardProgressSnapshot
import com.example.smartreview.data.model.LearningActivityType
import com.example.smartreview.data.model.LessonProgressSnapshot
import com.example.smartreview.data.model.QuizProgressSnapshot
import com.example.smartreview.data.model.ResumeLearningItem
import com.example.smartreview.data.model.UserLearningProgress
import com.example.smartreview.data.repository.FlashcardRepositoryProvider
import com.example.smartreview.data.repository.LearningProgressRepository
import com.example.smartreview.data.repository.LearningProgressRepositoryProvider
import com.example.smartreview.data.repository.LessonRepositoryProvider
import com.example.smartreview.data.repository.QuizRepositoryProvider
import com.example.smartreview.ui.navigation.Screen
import com.example.smartreview.ui.screens.lesson.lessonRoute
import com.example.smartreview.ui.screens.quiz.quizRoute

/**
 * Authenticated learning progress API (persist + resume). Gamification remains separate.
 */
class LearningProgressService(
    private val repository: LearningProgressRepository = LearningProgressRepositoryProvider.default,
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
        update { it.copy(quizInProgress = snapshot) }
    }

    suspend fun clearQuizInProgress() {
        update { it.copy(quizInProgress = null) }
    }

    suspend fun quizSnapshotForQuiz(quizId: String): QuizProgressSnapshot? =
        currentProgress()?.quizInProgress?.takeIf { it.quizId == quizId }

    suspend fun resumeLearningItems(): List<ResumeLearningItem> {
        val progress = currentProgress() ?: return emptyList()
        val items = mutableListOf<ResumeLearningItem>()

        progress.flashcardInProgress?.let { snapshot ->
            val deck = FlashcardRepositoryProvider.default.getDeck(snapshot.deckId)
                ?: FlashcardRepositoryProvider.default.getDefaultDeck()
            val total = deck.cards.size.coerceAtLeast(1)
            val studied = snapshot.knownCount + snapshot.reviewCount
            items += ResumeLearningItem(
                type = LearningActivityType.FLASHCARD,
                contentId = snapshot.deckId,
                title = deck.title,
                subtitle = "Flashcard · ${studied}/${total} thẻ",
                progressPercent = studied.toFloat() / total,
                imageUrl = "https://picsum.photos/seed/flashcard_resume/400/200",
                route = Screen.Flashcard.route,
            )
        }

        progress.lessonInProgress?.let { snapshot ->
            val lesson = LessonRepositoryProvider.default.getLesson(snapshot.lessonId) ?: return@let
            val total = lesson.blocks.size.coerceAtLeast(1)
            val viewed = snapshot.viewedBlockIds.size
            items += ResumeLearningItem(
                type = LearningActivityType.LESSON,
                contentId = snapshot.lessonId,
                title = lesson.title,
                subtitle = "Bài học · ${viewed}/${total} block",
                progressPercent = viewed.toFloat() / total,
                imageUrl = "https://picsum.photos/seed/lesson_${snapshot.lessonId}/400/200",
                route = lessonRoute(snapshot.lessonId),
            )
        }

        progress.quizInProgress?.let { snapshot ->
            val quiz = QuizRepositoryProvider.default.getQuiz(snapshot.quizId) ?: return@let
            val total = quiz.questions.size.coerceAtLeast(1)
            val answered = snapshot.answers.size
            items += ResumeLearningItem(
                type = LearningActivityType.QUIZ,
                contentId = snapshot.quizId,
                title = quiz.title,
                subtitle = "Quiz · ${answered}/${total} câu",
                progressPercent = answered.toFloat() / total,
                imageUrl = "https://picsum.photos/seed/quiz_${snapshot.quizId}/400/200",
                route = quizRoute(snapshot.quizId),
            )
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
