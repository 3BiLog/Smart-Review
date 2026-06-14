package com.example.smartreview.data.quiz

import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizAnswerRecord
import com.example.smartreview.data.model.QuizQuestion

object QuizScorer {

    fun evaluateAnswer(question: QuizQuestion, selectedOptionId: String): QuizAnswerRecord {
        val selectedIndex = selectedOptionId.toIntOrNull()
        val isCorrect = selectedIndex != null && selectedIndex == question.correctOptionIndex
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
        val passingScoreFloat = quiz.passingScore.toFloat() / 100f
        return QuizScore(
            correctCount = correct,
            totalQuestions = total,
            scorePercent = percent,
            passed = percent >= passingScoreFloat,
        )
    }
}

data class QuizScore(
    val correctCount: Int,
    val totalQuestions: Int,
    val scorePercent: Float,
    val passed: Boolean,
)