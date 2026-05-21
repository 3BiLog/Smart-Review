package com.example.smartreview.ui.screens.quizsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.quiz.QuizSessionStore
import com.example.smartreview.data.repository.GamificationServiceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizSummaryUiState(
    val quizTitle: String = "",
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val scorePercent: Float = 0f,
    val passed: Boolean = false,
    val studyTime: String = "00:00",
    val xpEarned: Int = 0,
    val streakDays: Int = 0,
    val rewardGranted: Boolean = false,
    val hasSessionData: Boolean = false,
)

class QuizSummaryViewModel(
    private val sessionId: String,
) : ViewModel() {

    private val gamificationService = GamificationServiceProvider.default

    private val _uiState = MutableStateFlow(QuizSummaryUiState())
    val uiState: StateFlow<QuizSummaryUiState> = _uiState.asStateFlow()

    init {
        val session = QuizSessionStore.consume(sessionId)
        if (session != null) {
            _uiState.update {
                it.copy(
                    quizTitle = session.quizTitle,
                    correctCount = session.correctCount,
                    totalQuestions = session.totalQuestions,
                    scorePercent = session.scorePercent,
                    passed = session.passed,
                    studyTime = session.formattedStudyTime(),
                    hasSessionData = true,
                )
            }
            viewModelScope.launch {
                LearningProgressServiceProvider.default.markQuizCompleted(session.quizId)
            }
            awardQuizXp(session.quizId)
        }
    }

    private fun awardQuizXp(quizId: String) {
        viewModelScope.launch {
            when (val result = gamificationService.rewardQuizComplete(quizId)) {
                is GamificationRewardResult.Success -> {
                    _uiState.update {
                        it.copy(
                            xpEarned = result.xpAwarded,
                            streakDays = result.newStreak,
                            rewardGranted = true,
                        )
                    }
                }
                is GamificationRewardResult.AlreadyProcessed -> {
                    _uiState.update { it.copy(rewardGranted = false) }
                }
                else -> Unit
            }
        }
    }

    companion object {
        fun provideFactory(sessionId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    QuizSummaryViewModel(sessionId) as T
            }
    }
}
