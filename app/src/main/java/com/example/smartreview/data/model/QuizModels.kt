package com.example.smartreview.data.model

import com.google.firebase.Timestamp

/**
 * Quiz domain models - Updated to match Firestore schema from Web Admin
 */

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String = "",
)

@Deprecated("Use QuizQuestion with options List<String> instead")
data class QuizOption(
    val id: String,
    val label: String,
)

data class Quiz(
    val id: String,
    val title: String,
    val description: String = "",
    val lessonId: String? = null,
    val courseId: String? = null,
    val moduleId: String? = null,
    val questions: List<QuizQuestion>,
    val passingScore: Int = 70,
    val xpReward: Long = 50,
    val duration: Long = 0,
)

// REMOVED: QuizAnswerRecord - now defined in LearningProgressModels.kt
// REMOVED: QuizProgressSnapshot - now defined in LearningProgressModels.kt

data class QuizCompletionResult(
    val sessionId: String,
    val quizId: String,
    val quizTitle: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercent: Int,
    val passed: Boolean,
    val durationMs: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val xpEarned: Long = 0,
    val lessonId: String = "",
) {
    fun formattedStudyTime(): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}

fun QuizQuestion.toLegacyFormat(): Pair<String, List<QuizOption>> {
    val optionsList = options.mapIndexed { index, label ->
        QuizOption(id = "${this.id}_opt_$index", label = label)
    }
    val correctOptionId = optionsList.getOrNull(correctOptionIndex)?.id ?: ""
    return correctOptionId to optionsList
}