package com.example.smartreview.data.model

/**
 * Local-first quiz domain models.
 */
data class QuizOption(
    val id: String,
    val label: String,
)

data class QuizQuestion(
    val id: String,
    val prompt: String,
    val options: List<QuizOption>,
    val correctOptionId: String,
    val explanation: String = "",
)

data class Quiz(
    val id: String,
    val title: String,
    val subtitle: String,
    val lessonId: String? = null,
    val questions: List<QuizQuestion>,
    val passingScore: Float = 0.6f,
)

data class QuizAnswerRecord(
    val questionId: String,
    val selectedOptionId: String,
    val isCorrect: Boolean,
)

data class QuizCompletionResult(
    val sessionId: String,
    val quizId: String,
    val quizTitle: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercent: Float,
    val passed: Boolean,
    val durationMs: Long,
    val completedAt: Long = System.currentTimeMillis(),
) {
    fun formattedStudyTime(): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}
