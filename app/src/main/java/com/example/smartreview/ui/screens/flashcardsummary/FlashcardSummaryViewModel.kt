package com.example.smartreview.ui.screens.flashcardsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── Data model ──────────────────────────────────────────────────────────────

data class FlashcardSummaryUiState(
    /** XP earned in this session */
    val xpEarned: Int = 30,

    /** 0f → 1f, e.g. 0.8f = 80 % */
    val accuracy: Float = 0.80f,

    /** Number of cards marked "known" */
    val knownCount: Int = 24,

    /** Number of cards to review again */
    val reviewCount: Int = 6,

    /** Consecutive study-day streak */
    val streakDays: Int = 5,

    /** Study time formatted as "mm:ss" */
    val studyTime: String = "12:45",

    /**
     * Controls the animated ring progress (starts at 0, animates to [accuracy]).
     * Separated so we can drive a smooth launch animation without touching the
     * rest of the state.
     */
    val animatedAccuracy: Float = 0f,

    /** True while the "next" navigation is processing */
    val isNavigating: Boolean = false,
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class FlashcardSummaryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardSummaryUiState())
    val uiState: StateFlow<FlashcardSummaryUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
        animateAccuracyRing()
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    fun onNextClicked(onNavigate: () -> Unit) {
        if (_uiState.value.isNavigating) return
        _uiState.update { it.copy(isNavigating = true) }
        viewModelScope.launch {
            delay(300)                // brief ripple feedback
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

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * In a real app this would accept a [FlashcardResult] from the
     * previous screen (passed via SavedStateHandle or shared ViewModel).
     * Here we keep mock data so the screen runs standalone.
     */
    private fun loadMockData() {
        _uiState.update {
            it.copy(
                xpEarned    = 30,
                accuracy    = 0.80f,
                knownCount  = 24,
                reviewCount = 6,
                streakDays  = 5,
                studyTime   = "12:45",
            )
        }
    }

    /** Animate the SVG-style ring from 0 → target accuracy over ~900 ms */
    private fun animateAccuracyRing() {
        viewModelScope.launch {
            val target  = _uiState.value.accuracy
            val steps   = 60
            val delayMs = 900L / steps
            repeat(steps) { i ->
                delay(delayMs)
                _uiState.update { it.copy(animatedAccuracy = target * (i + 1f) / steps) }
            }
            // clamp to exact value
            _uiState.update { it.copy(animatedAccuracy = target) }
        }
    }
}
