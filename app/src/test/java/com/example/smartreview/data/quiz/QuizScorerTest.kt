package com.example.smartreview.data.quiz

import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizOption
import com.example.smartreview.data.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScorerTest {

    private val sampleQuiz = Quiz(
        id = "q_test",
        title = "Test",
        subtitle = "Test",
        questions = listOf(
            QuizQuestion(
                id = "1",
                prompt = "P1",
                options = listOf(QuizOption("a", "A"), QuizOption("b", "B")),
                correctOptionId = "a",
            ),
            QuizQuestion(
                id = "2",
                prompt = "P2",
                options = listOf(QuizOption("a", "A"), QuizOption("b", "B")),
                correctOptionId = "b",
            ),
        ),
        passingScore = 0.5f,
    )

    @Test
    fun evaluateAnswer_detectsCorrect() {
        val record = QuizScorer.evaluateAnswer(sampleQuiz.questions[0], "a")
        assertTrue(record.isCorrect)
    }

    @Test
    fun scoreQuiz_calculatesPercentAndPass() {
        val answers = listOf(
            QuizScorer.evaluateAnswer(sampleQuiz.questions[0], "a"),
            QuizScorer.evaluateAnswer(sampleQuiz.questions[1], "a"),
        )
        val score = QuizScorer.scoreQuiz(sampleQuiz, answers)
        assertEquals(1, score.correctCount)
        assertEquals(0.5f, score.scorePercent, 0.001f)
        assertTrue(score.passed)
    }

    @Test
    fun scoreQuiz_failsBelowThreshold() {
        val answers = listOf(
            QuizScorer.evaluateAnswer(sampleQuiz.questions[0], "b"),
            QuizScorer.evaluateAnswer(sampleQuiz.questions[1], "a"),
        )
        val score = QuizScorer.scoreQuiz(sampleQuiz, answers)
        assertEquals(0, score.correctCount)
        assertFalse(score.passed)
    }
}
