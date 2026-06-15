package com.example.smartreview.ui.screens.quizsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.quiz.QuizSessionStore
import com.example.smartreview.data.repository.GamificationServiceProvider
import com.example.smartreview.data.repository.LessonRepositoryProvider
import com.example.smartreview.data.repository.QuizRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class QuizSummaryUiState(
    val courseId: String = "",
    val quizTitle: String = "",
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val scorePercent: Float = 0f,
    val passed: Boolean = false,
    val studyTime: String = "00:00",
    val xpEarned: Int = 0,
    val streakDays: Int = 0,
    val rewardGranted: Boolean = false,
    val rewardMessage: String? = null,
    val hasSessionData: Boolean = false,
    val isLoading: Boolean = true,  // ✅ Thêm loading state
)

class QuizSummaryViewModel(
    private val sessionId: String,
) : ViewModel() {

    private val gamificationService = GamificationServiceProvider.default

    private val _uiState = MutableStateFlow(QuizSummaryUiState(isLoading = true))
    val uiState: StateFlow<QuizSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
    }

    // ✅ Sửa: Tách logic vào suspend function
    private fun loadSessionData() {
        viewModelScope.launch {
            val session = QuizSessionStore.consume(sessionId)
            if (session != null) {
                val courseId = resolveCourseId(session.quizId)
                _uiState.update {
                    it.copy(
                        courseId = courseId,
                        quizTitle = session.quizTitle,
                        correctCount = session.correctCount,
                        totalQuestions = session.totalQuestions,
                        scorePercent = session.scorePercent.toFloat(),
                        passed = session.passed,
                        studyTime = session.formattedStudyTime(),
                        hasSessionData = true,
                        isLoading = false,
                    )
                }
                LearningProgressServiceProvider.default.markQuizCompleted(session.quizId)
                awardQuizXp(session.quizId)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun awardQuizXp(quizId: String) {
        when (val result = gamificationService.rewardQuizComplete(quizId)) {
            is GamificationRewardResult.Success -> {
                _uiState.update {
                    it.copy(
                        xpEarned = result.xpAwarded,
                        streakDays = result.newStreak,
                        rewardGranted = true,
                        rewardMessage = null,
                    )
                }
            }
            is GamificationRewardResult.AlreadyProcessed -> {
                _uiState.update {
                    it.copy(
                        rewardGranted = false,
                        rewardMessage = "XP quiz đã được nhận trước đó (mỗi quiz một lần).",
                    )
                }
            }
            is GamificationRewardResult.Failed -> {
                _uiState.update {
                    it.copy(
                        rewardGranted = false,
                        rewardMessage = "Không thể cộng XP lên Firestore. Thử lại sau.",
                    )
                }
            }
            else -> Unit
        }
    }

    // ✅ Sửa: resolveCourseId là suspend function
    private suspend fun resolveCourseId(quizId: String): String {
        val quiz = QuizRepositoryProvider.default.getQuiz(quizId) ?: return ""
        val lessonId = quiz.lessonId ?: return ""
        return LessonRepositoryProvider.default.getLesson(lessonId)?.courseId.orEmpty()
    }

    companion object {
        fun provideFactory(sessionId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return QuizSummaryViewModel(sessionId) as T
                }
            }
    }
}