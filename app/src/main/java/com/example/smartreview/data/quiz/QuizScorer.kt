package com.example.smartreview.data.quiz

import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizAnswerRecord
import com.example.smartreview.data.model.QuizQuestion

/**
 * Pure scoring for quiz answers.
 */
object QuizScorer {

    fun evaluateAnswer(question: QuizQuestion, selectedOptionId: String): QuizAnswerRecord {
        val isCorrect = selectedOptionId == question.correctOptionId
        return QuizAnswerRecord(
            questionId = question.id,
            selectedOptionId = selectedOptionId,
            isCorrect = isCorrect,
        )
    }

    fun scoreQuiz(quiz: Quiz, answers: List<QuizAnswerRecord>): QuizScore {
        val total = quiz.questions.size.coerceAtLeast(1)
        val correct = answers.count { it.isCorrect }
        val percent = correct.toFloat() / total
        return QuizScore(
            correctCount = correct,
            totalQuestions = total,
            scorePercent = percent,
            passed = percent >= quiz.passingScore,
        )
    }
}

data class QuizScore(
    val correctCount: Int,
    val totalQuestions: Int,
    val scorePercent: Float,
    val passed: Boolean,
)
