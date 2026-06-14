package com.example.smartreview.data.model

import com.google.firebase.Timestamp

/**
 * Quiz domain models - Updated to match Firestore schema from Web Admin
 *
 * Firestore structure:
 * courses/{courseId}/modules/{moduleId}/lessons/{lessonId}
 *   type: "quiz"
 *   content.data {
 *     passingScore: 70,
 *     questions: [{
 *       id, text, options (List<String>), correctOptionIndex, explanation
 *     }]
 *   }
 */

// Updated to match Firestore (options are List<String>, not List<QuizOption>)
data class QuizQuestion(
    val id: String,
    val text: String,           // "prompt" in old code
    val options: List<String>,  // Changed from List<QuizOption>
    val correctOptionIndex: Int, // Changed from correctOptionId
    val explanation: String = "",
)

// Keep for backward compatibility but deprecate
@Deprecated("Use QuizQuestion with options List<String> instead")
data class QuizOption(
    val id: String,
    val label: String,
)

data class Quiz(
    val id: String,
    val title: String,
    val description: String = "",  // was "subtitle"
    val lessonId: String? = null,
    val courseId: String? = null,
    val moduleId: String? = null,
    val questions: List<QuizQuestion>,
    val passingScore: Int = 70,    // Changed from Float to Int
    val xpReward: Long = 50,
    val duration: Long = 0,
)

data class QuizAnswerRecord(
    val questionId: String,
    val selectedOptionIndex: Int,   // Changed from selectedOptionId
    val isCorrect: Boolean,
)

data class QuizCompletionResult(
    val sessionId: String,
    val quizId: String,
    val quizTitle: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercent: Int,           // Changed from Float to Int
    val passed: Boolean,
    val durationMs: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val xpEarned: Long = 0,
) {
    fun formattedStudyTime(): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}

// For progress persistence
data class QuizProgressSnapshot(
    val quizId: String,
    val sessionId: String,
    val sessionStartedAt: Long,
    val currentIndex: Int,
    val answers: List<QuizAnswerRecord>,
    val selectedOptionIndex: Int?,   // Changed from selectedOptionId
    val showFeedback: Boolean,
)

// Extension function to convert from Firestore question format
fun QuizQuestion.toLegacyFormat(): Pair<String, List<QuizOption>> {
    val optionsList = options.mapIndexed { index, label ->
        QuizOption(id = "${this.id}_opt_$index", label = label)
    }
    val correctOptionId = optionsList.getOrNull(correctOptionIndex)?.id ?: ""
    return correctOptionId to optionsList
}