package com.example.smartreview.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizAnswerRecord
import com.example.smartreview.data.model.QuizCompletionResult
import com.example.smartreview.data.model.QuizProgressSnapshot
import com.example.smartreview.data.model.QuizQuestion
import com.example.smartreview.data.quiz.QuizScorer
import com.example.smartreview.data.quiz.QuizSessionStore
import com.example.smartreview.data.repository.QuizRepository
import com.example.smartreview.data.repository.QuizRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class QuizUiState(
    val quiz: Quiz? = null,
    val currentIndex: Int = 0,
    val selectedOptionId: String? = null,
    val showFeedback: Boolean = false,
    val lastFeedbackCorrect: Boolean = false,
    val lastExplanation: String = "",
    val answers: List<QuizAnswerRecord> = emptyList(),
    val isQuizFinished: Boolean = false,
    val isLoading: Boolean = true,
    val isResuming: Boolean = false,
    val alreadyCompleted: Boolean = false,
)

class QuizViewModel(
    private val quizId: String,
    private val quizRepository: QuizRepository = QuizRepositoryProvider.default,
) : ViewModel() {

    private var sessionId: String = UUID.randomUUID().toString()
    private var sessionStartedAt: Long = System.currentTimeMillis()
    private val answerRecords = mutableListOf<QuizAnswerRecord>()
    private val progressService = LearningProgressServiceProvider.default

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadQuiz() }
    }

    val totalQuestions: Int get() = _uiState.value.quiz?.questions?.size ?: 0

    val currentQuestion: QuizQuestion?
        get() = _uiState.value.quiz?.questions?.getOrNull(_uiState.value.currentIndex)

    fun selectOption(optionId: String) {
        if (_uiState.value.showFeedback || _uiState.value.isQuizFinished) return
        _uiState.update { it.copy(selectedOptionId = optionId) }
        persistProgress()
    }

    fun submitAnswer() {
        val state = _uiState.value
        val question = currentQuestion ?: return
        val selected = state.selectedOptionId ?: return
        if (state.showFeedback) return

        val record = QuizScorer.evaluateAnswer(question, selected)
        answerRecords.add(record)

        _uiState.update {
            it.copy(
                showFeedback = true,
                lastFeedbackCorrect = record.isCorrect,
                lastExplanation = question.explanation,
                answers = answerRecords.toList(),
            )
        }
        persistProgress()
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.showFeedback) return

        val quiz = state.quiz ?: return
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= quiz.questions.size) {
            _uiState.update {
                it.copy(
                    isQuizFinished = true,
                    showFeedback = false,
                    selectedOptionId = null,
                )
            }
            viewModelScope.launch { progressService.clearQuizInProgress() }
        } else {
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    showFeedback = false,
                    selectedOptionId = null,
                    lastExplanation = "",
                )
            }
        }
        persistProgress()
    }

    fun previousQuestion() {
        if (_uiState.value.showFeedback || _uiState.value.isQuizFinished) return
        val prev = (_uiState.value.currentIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentIndex = prev, selectedOptionId = null) }
        persistProgress()
    }

    fun completeQuiz(): String? {
        val quiz = _uiState.value.quiz ?: return null
        if (!_uiState.value.isQuizFinished) return null

        val score = QuizScorer.scoreQuiz(quiz, answerRecords)
        val result = QuizCompletionResult(
            sessionId = sessionId,
            quizId = quiz.id,
            quizTitle = quiz.title,
            totalQuestions = score.totalQuestions,
            correctCount = score.correctCount,
            scorePercent = score.scorePercent,
            passed = score.passed,
            durationMs = System.currentTimeMillis() - sessionStartedAt,
        )
        QuizSessionStore.put(result)
        return sessionId
    }

    private suspend fun loadQuiz() {
        val quiz = quizRepository.getQuiz(quizId)
        if (quiz == null) {
            _uiState.value = QuizUiState(isLoading = false)
            return
        }

        var isResuming = false
        val snapshot = progressService.quizSnapshotForQuiz(quizId)
        if (snapshot != null) {
            isResuming = true
            sessionId = snapshot.sessionId
            sessionStartedAt = snapshot.sessionStartedAt
            answerRecords.clear()
            answerRecords.addAll(snapshot.answers)
            _uiState.value = QuizUiState(
                quiz = quiz,
                currentIndex = snapshot.currentIndex,
                selectedOptionId = snapshot.selectedOptionId,
                showFeedback = snapshot.showFeedback,
                answers = answerRecords.toList(),
                isLoading = false,
                isResuming = true,
                alreadyCompleted = progressService.isQuizCompleted(quizId),
            )
            return
        }

        _uiState.value = QuizUiState(
            quiz = quiz,
            isLoading = false,
            alreadyCompleted = progressService.isQuizCompleted(quizId),
            isResuming = isResuming,
        )
    }

    private fun persistProgress() {
        val state = _uiState.value
        val quiz = state.quiz ?: return
        if (state.isQuizFinished) return
        viewModelScope.launch {
            progressService.saveQuizSnapshot(
                QuizProgressSnapshot(
                    quizId = quiz.id,
                    sessionId = sessionId,
                    sessionStartedAt = sessionStartedAt,
                    currentIndex = state.currentIndex,
                    answers = answerRecords.toList(),
                    selectedOptionId = state.selectedOptionId,
                    showFeedback = state.showFeedback,
                ),
            )
        }
    }

    companion object {
        fun provideFactory(quizId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    QuizViewModel(quizId) as T
            }
    }
}
