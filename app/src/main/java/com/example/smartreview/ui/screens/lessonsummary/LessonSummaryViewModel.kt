package com.example.smartreview.ui.screens.lessonsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.lesson.LessonSessionStore
import com.example.smartreview.data.model.LessonCompletionResult
import com.example.smartreview.data.repository.GamificationServiceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LessonSummaryUiState(
    val lessonTitle: String = "",
    val xpEarned: Int = 0,
    val streakDays: Int = 0,
    val viewedBlocks: Int = 0,
    val totalBlocks: Int = 0,
    val studyTime: String = "00:00",
    val progress: Float = 0f,
    val rewardGranted: Boolean = false,
    val hasSessionData: Boolean = false,
)

class LessonSummaryViewModel(
    private val sessionId: String,
) : ViewModel() {

    private val gamificationService = GamificationServiceProvider.default

    private val _uiState = MutableStateFlow(LessonSummaryUiState())
    val uiState: StateFlow<LessonSummaryUiState> = _uiState.asStateFlow()

    init {
        val session = LessonSessionStore.consume(sessionId)
        if (session != null) {
            applySession(session)
            awardLessonXp(session.lessonId)
        }
    }

    private fun applySession(session: LessonCompletionResult) {
        _uiState.update {
            it.copy(
                lessonTitle = session.lessonTitle,
                viewedBlocks = session.viewedBlocks,
                totalBlocks = session.totalBlocks,
                studyTime = session.formattedStudyTime(),
                progress = session.progress,
                hasSessionData = true,
            )
        }
    }

    private fun awardLessonXp(lessonId: String) {
        viewModelScope.launch {
            when (val result = gamificationService.rewardLessonComplete(lessonId)) {
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
                    LessonSummaryViewModel(sessionId) as T
            }
    }
}
