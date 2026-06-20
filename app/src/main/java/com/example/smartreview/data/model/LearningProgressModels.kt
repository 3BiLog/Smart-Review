package com.example.smartreview.data.model

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

data class QuizAnswerRecord(
    val questionId: String,
    val selectedOptionId: String,
    val isCorrect: Boolean,
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

data class LearningProgressionItem(
    val type: LearningActivityType,
    val contentId: String,
    val title: String,
    val progressPercent: Float,
    val imageUrl: String,
    val route: String,
    val courseId: String? = null,
    val courseTitle: String? = null,
    val moduleId: String? = null,
    val moduleTitle: String? = null,
    val lessonId: String? = null,
    val progressDetail: String = "",
    val lastActivityAt: Long = 0L,
) {
    val subtitle: String
        get() = buildList {
            courseTitle?.takeIf { it.isNotBlank() }?.let { add(it) }
            moduleTitle?.takeIf { it.isNotBlank() }?.let { add(it) }
            progressDetail.takeIf { it.isNotBlank() }?.let { add(it) }
        }.joinToString(" · ")
}

typealias ResumeLearningItem = LearningProgressionItem
