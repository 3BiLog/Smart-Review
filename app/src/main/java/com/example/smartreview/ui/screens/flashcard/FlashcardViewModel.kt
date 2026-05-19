package com.example.smartreview.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Flashcard(
    val question:  String,
    val keyword:   String,
    val answer:    String,
)

data class FlashcardUiState(
    val deckTitle:    String         = "Flashcard: UI Design",
    val cards:        List<Flashcard> = emptyList(),
    val currentIndex: Int            = 0,
    val isFlipped:    Boolean        = false,
    val knownCount:   Int            = 0,
    val repeatCount:  Int            = 0,
)

class FlashcardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = FlashcardUiState(
            cards = listOf(
                Flashcard(
                    question = "What is Glassmorphism?",
                    keyword  = "Glassmorphism",
                    answer   = "A design style that uses frosted-glass effects — semi-transparent backgrounds with blur, subtle borders and soft shadows — to create a sense of depth and hierarchy in UI."
                ),
                Flashcard(
                    question = "What is a Design System?",
                    keyword  = "Design System",
                    answer   = "A collection of reusable components, guided by clear standards, that can be assembled together to build any number of applications — ensuring consistency across products."
                ),
                Flashcard(
                    question = "Define UX Research.",
                    keyword  = "UX Research",
                    answer   = "The systematic study of target users and their requirements to add realistic context and insight to design processes through methods like interviews, surveys and usability tests."
                ),
            )
        )
    }

    val total get() = _uiState.value.cards.size

    fun flip()   = _uiState.update { it.copy(isFlipped = !it.isFlipped) }

    fun markKnown() {
        _uiState.update { it.copy(knownCount = it.knownCount + 1, isFlipped = false) }
        advance()
    }

    fun markRepeat() {
        _uiState.update { it.copy(repeatCount = it.repeatCount + 1, isFlipped = false) }
        advance()
    }

    private fun advance() {
        val next = (_uiState.value.currentIndex + 1) % _uiState.value.cards.size
        _uiState.update { it.copy(currentIndex = next) }
    }
}