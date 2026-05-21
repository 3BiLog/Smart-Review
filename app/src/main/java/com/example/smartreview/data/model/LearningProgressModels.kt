package com.example.smartreview.data.model

/**
 * Persistent learning state per authenticated user (local-first; Room/Firestore later).
 */
enum class LearningActivityType {
    FLASHCARD,
    LESSON,
    QUIZ,
}

data class FlashcardProgressSnapshot(
    val deckId: String,
    val sessionId: String,
    val sessionStartedAt: Long,
    val currentIndex: Int,
    val cardStatuses: Map<String, String>,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
)

data class LessonProgressSnapshot(
    val lessonId: String,
    val sessionId: String,
    val sessionStartedAt: Long,
    val currentBlockIndex: Int,
    val viewedBlockIds: Set<String>,
)

data class QuizProgressSnapshot(
    val quizId: String,
    val sessionId: String,
    val sessionStartedAt: Long,
    val currentIndex: Int,
    val answers: List<QuizAnswerRecord>,
    val selectedOptionId: String? = null,
    val showFeedback: Boolean = false,
)

data class UserLearningProgress(
    val uid: String,
    val completedLessonIds: Set<String> = emptySet(),
    val completedQuizIds: Set<String> = emptySet(),
    val flashcardInProgress: FlashcardProgressSnapshot? = null,
    val lessonInProgress: LessonProgressSnapshot? = null,
    val quizInProgress: QuizProgressSnapshot? = null,
    val lastUpdatedAt: Long = System.currentTimeMillis(),
)

data class ResumeLearningItem(
    val type: LearningActivityType,
    val contentId: String,
    val title: String,
    val subtitle: String,
    val progressPercent: Float,
    val imageUrl: String,
    val route: String,
)
