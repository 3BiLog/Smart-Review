package com.example.smartreview.ui.screens.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.flashcard.FlashcardSessionStore
import com.example.smartreview.data.learning.LearningProgressServiceProvider
import com.example.smartreview.data.model.CardStudyStatus
import com.example.smartreview.data.model.FlashcardCard
import com.example.smartreview.data.model.FlashcardProgressSnapshot
import com.example.smartreview.data.model.FlashcardSessionResult
import com.example.smartreview.data.mock.MockFlashcardData
import com.example.smartreview.data.repository.FlashcardRepository
import com.example.smartreview.data.repository.FlashcardRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class FlashcardUiCard(
    val id: String,
    val question: String,
    val keyword: String,
    val answer: String,
)

data class FlashcardUiState(
    val deckId: String = "",
    val deckTitle: String = "",
    val cards: List<FlashcardUiCard> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val knownCount: Int = 0,
    val reviewCount: Int = 0,
    val studiedCount: Int = 0,
    val remainingCount: Int = 0,
    val isSessionComplete: Boolean = false,
    val isLoading: Boolean = true,
    val isResuming: Boolean = false,
)

class FlashcardViewModel(
    private val deckId: String = MockFlashcardData.DEFAULT_DECK_ID,
    private val flashcardRepository: FlashcardRepository = FlashcardRepositoryProvider.default,
) : ViewModel() {

    private var sessionId: String = UUID.randomUUID().toString()
    private var sessionStartedAt: Long = System.currentTimeMillis()
    private val cardStatuses = mutableMapOf<String, CardStudyStatus>()
    private val progressService = LearningProgressServiceProvider.default

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadDeck() }
    }

    val total: Int get() = _uiState.value.cards.size

    fun flip() = _uiState.update { it.copy(isFlipped = !it.isFlipped) }

    fun markKnown() = recordAnswer(CardStudyStatus.KNOWN)

    fun markRepeat() = recordAnswer(CardStudyStatus.REPEAT)

    fun previousCard() {
        if (_uiState.value.isSessionComplete) return
        val prev = (_uiState.value.currentIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(currentIndex = prev, isFlipped = false) }
        persistProgress()
    }

    fun nextCard() {
        if (_uiState.value.isSessionComplete) return
        val cards = _uiState.value.cards
        if (cards.isEmpty()) return
        val next = (_uiState.value.currentIndex + 1).coerceAtMost(cards.lastIndex)
        _uiState.update { it.copy(currentIndex = next, isFlipped = false) }
        persistProgress()
    }

    fun completeSession(): String? {
        val state = _uiState.value
        if (!state.isSessionComplete || state.cards.isEmpty()) return null

        val result = FlashcardSessionResult(
            sessionId = sessionId,
            deckId = state.deckId,
            deckTitle = state.deckTitle,
            totalCards = state.cards.size,
            knownCount = state.knownCount,
            reviewCount = state.reviewCount,
            studiedCount = state.studiedCount,
            durationMs = System.currentTimeMillis() - sessionStartedAt,
        )
        FlashcardSessionStore.put(result)
        viewModelScope.launch { progressService.clearFlashcardInProgress() }
        return sessionId
    }

    private suspend fun loadDeck() {
        val deck = flashcardRepository.getDeck(deckId)
            ?: flashcardRepository.getDefaultDeck()
        cardStatuses.clear()
        deck.cards.forEach { card -> cardStatuses[card.id] = CardStudyStatus.UNSEEN }

        var isResuming = false
        val snapshot = progressService.flashcardSnapshotForDeck(deck.id)
        if (snapshot != null) {
            isResuming = true
            sessionId = snapshot.sessionId
            sessionStartedAt = snapshot.sessionStartedAt
            cardStatuses.clear()
            progressService.cardStatusFromSnapshot(snapshot.cardStatuses).forEach { (id, status) ->
                cardStatuses[id] = status
            }
        }

        _uiState.value = FlashcardUiState(
            deckId = deck.id,
            deckTitle = deck.title,
            cards = deck.cards.map { it.toUiCard() },
            currentIndex = snapshot?.currentIndex ?: 0,
            isLoading = false,
            isResuming = isResuming,
        )
        publishProgress(isFlipped = false)
        if (_uiState.value.remainingCount == 0) {
            _uiState.update { it.copy(isSessionComplete = true) }
        }
    }

    private fun recordAnswer(status: CardStudyStatus) {
        val state = _uiState.value
        if (state.isSessionComplete || state.cards.isEmpty()) return

        val card = state.cards.getOrNull(state.currentIndex) ?: return
        if (cardStatuses[card.id] != CardStudyStatus.UNSEEN) {
            _uiState.update { it.copy(isFlipped = false) }
            moveToNextUnseen(fromIndex = state.currentIndex)
            persistProgress()
            return
        }

        cardStatuses[card.id] = status
        publishProgress(isFlipped = false)

        if (_uiState.value.remainingCount == 0) {
            _uiState.update { it.copy(isSessionComplete = true) }
        } else {
            moveToNextUnseen(fromIndex = state.currentIndex)
        }
        persistProgress()
    }

    private fun moveToNextUnseen(fromIndex: Int) {
        val cards = _uiState.value.cards
        if (cards.isEmpty()) return
        val order = (fromIndex + 1 until cards.size) + (0 until fromIndex)
        val nextIndex = order.firstOrNull { index ->
            cardStatuses[cards[index].id] == CardStudyStatus.UNSEEN
        } ?: fromIndex
        _uiState.update { it.copy(currentIndex = nextIndex) }
    }

    private fun publishProgress(isFlipped: Boolean) {
        val cards = _uiState.value.cards
        var known = 0
        var review = 0
        var studied = 0
        cards.forEach { card ->
            when (cardStatuses[card.id] ?: CardStudyStatus.UNSEEN) {
                CardStudyStatus.KNOWN -> {
                    known++
                    studied++
                }
                CardStudyStatus.REPEAT -> {
                    review++
                    studied++
                }
                CardStudyStatus.UNSEEN -> Unit
            }
        }
        _uiState.update {
            it.copy(
                knownCount = known,
                reviewCount = review,
                studiedCount = studied,
                remainingCount = (cards.size - studied).coerceAtLeast(0),
                isFlipped = isFlipped,
            )
        }
    }

    private fun persistProgress() {
        val state = _uiState.value
        if (state.cards.isEmpty() || state.isSessionComplete) return
        viewModelScope.launch {
            progressService.saveFlashcardSnapshot(
                FlashcardProgressSnapshot(
                    deckId = state.deckId,
                    sessionId = sessionId,
                    sessionStartedAt = sessionStartedAt,
                    currentIndex = state.currentIndex,
                    cardStatuses = progressService.cardStatusToSnapshot(cardStatuses),
                    knownCount = state.knownCount,
                    reviewCount = state.reviewCount,
                ),
            )
        }
    }

    private fun FlashcardCard.toUiCard() = FlashcardUiCard(
        id = id,
        question = question,
        keyword = keyword,
        answer = answer,
    )

    companion object {
        fun provideFactory(deckId: String? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FlashcardViewModel(deckId = deckId ?: MockFlashcardData.DEFAULT_DECK_ID) as T
            }
    }
}
