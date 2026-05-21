package com.example.smartreview.ui.screens.flashcardsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.flashcard.FlashcardSessionStore
import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.model.FlashcardSessionResult
import com.example.smartreview.data.repository.GamificationServiceProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashcardSummaryUiState(
    val xpEarned: Int = 0,
    val accuracy: Float = 0f,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val streakDays: Int = 0,
    val studyTime: String = "00:00",
    val animatedAccuracy: Float = 0f,
    val isNavigating: Boolean = false,
    val hasSessionData: Boolean = false,
)

class FlashcardSummaryViewModel(
    private val sessionId: String,
) : ViewModel() {

    private val gamificationService = GamificationServiceProvider.default

    private val _uiState = MutableStateFlow(FlashcardSummaryUiState())
    val uiState: StateFlow<FlashcardSummaryUiState> = _uiState.asStateFlow()

    init {
        val session = FlashcardSessionStore.consume(sessionId)
        if (session != null) {
            applySession(session)
            awardFlashcardXp(session.sessionId)
        }
        animateAccuracyRing()
    }

    fun onNextClicked(onNavigate: () -> Unit) {
        if (_uiState.value.isNavigating) return
        _uiState.update { it.copy(isNavigating = true) }
        viewModelScope.launch {
            delay(300)
            onNavigate()
            _uiState.update { it.copy(isNavigating = false) }
        }
    }

    fun onReviewClicked(onNavigate: () -> Unit) {
        viewModelScope.launch {
            delay(200)
            onNavigate()
        }
    }

    private fun applySession(session: FlashcardSessionResult) {
        _uiState.update {
            it.copy(
                accuracy = session.accuracy,
                knownCount = session.knownCount,
                reviewCount = session.reviewCount,
                studyTime = session.formattedStudyTime(),
                hasSessionData = true,
            )
        }
    }

    private fun awardFlashcardXp(rewardSessionId: String) {
        viewModelScope.launch {
            when (val result = gamificationService.rewardFlashcardSession(rewardSessionId)) {
                is GamificationRewardResult.Success -> {
                    _uiState.update {
                        it.copy(
                            xpEarned = result.xpAwarded,
                            streakDays = result.newStreak,
                        )
                    }
                }
                is GamificationRewardResult.AlreadyProcessed -> Unit
                else -> Unit
            }
        }
    }

    private fun animateAccuracyRing() {
        viewModelScope.launch {
            val target = _uiState.value.accuracy
            val steps = 60
            val delayMs = 900L / steps
            repeat(steps) { i ->
                delay(delayMs)
                _uiState.update { it.copy(animatedAccuracy = target * (i + 1f) / steps) }
            }
            _uiState.update { it.copy(animatedAccuracy = target) }
        }
    }

    companion object {
        fun provideFactory(sessionId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FlashcardSummaryViewModel(sessionId) as T
            }
    }
}
