package com.example.smartreview.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val showFeedback: Boolean = false,
    val lastFeedbackCorrect: Boolean = false,
    val selectedOptionId: String? = null,
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

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        android.util.Log.d("QuizViewModel", ">>> Initializing with quizId=$quizId")
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
        val selectedOptionId = state.selectedOptionId ?: return
        if (state.showFeedback) return

        val selectedIndex = selectedOptionId.toIntOrNull()
        val isCorrect = selectedIndex != null && selectedIndex == question.correctOptionIndex

        val record = QuizAnswerRecord(
            questionId = question.id,
            selectedOptionId = selectedOptionId,
            isCorrect = isCorrect
        )
        answerRecords.add(record)
        val quiz = state.quiz ?: return
        val isLastQuestion = state.currentIndex >= quiz.questions.size - 1

        _uiState.update {
            it.copy(
                showFeedback = true,
                lastFeedbackCorrect = isCorrect,
                lastExplanation = question.explanation,
                answers = answerRecords.toList(),
                isQuizFinished = isLastQuestion,
                selectedOptionId = if (isLastQuestion) null else it.selectedOptionId,
            )
        }
        if (isLastQuestion) {
            // Clear progress if needed
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
        _uiState.update {
            it.copy(
                currentIndex = prev,
                selectedOptionId = null,
                showFeedback = false
            )
        }
        persistProgress()
    }

    fun completeQuiz(): QuizCompletionResult? {
        val quiz = _uiState.value.quiz ?: return null
        if (!_uiState.value.isQuizFinished) return null

        val correctCount = answerRecords.count { it.isCorrect }
        val total = quiz.questions.size
        val scorePercent = if (total > 0) (correctCount * 100 / total) else 0
        val passed = scorePercent >= quiz.passingScore

        val result = QuizCompletionResult(
            sessionId = sessionId,
            quizId = quiz.id,
            quizTitle = quiz.title,
            totalQuestions = total,
            correctCount = correctCount,
            scorePercent = scorePercent,
            passed = passed,
            durationMs = System.currentTimeMillis() - sessionStartedAt,
            xpEarned = if (passed) quiz.xpReward else 0
        )
        QuizSessionStore.put(result)
        return result
    }

    private suspend fun loadQuiz() {
        _uiState.update { it.copy(isLoading = true) }
        android.util.Log.d("QuizViewModel", "Loading quiz with ID: $quizId")
        val quiz = quizRepository.getQuiz(quizId)
        android.util.Log.d("QuizViewModel", "Quiz loaded: ${quiz != null}, questions=${quiz?.questions?.size}")
        if (quiz == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        _uiState.update {
            it.copy(
                quiz = quiz,
                isLoading = false,
                alreadyCompleted = false,
                isResuming = false,
            )
        }
        persistProgress()
    }

    private fun persistProgress() {
        val state = _uiState.value
        val quiz = state.quiz ?: return
        if (state.isQuizFinished) return
        // Save to local storage if needed
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