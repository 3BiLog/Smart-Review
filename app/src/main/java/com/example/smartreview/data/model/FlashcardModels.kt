package com.example.smartreview.data.model

/**
 * Local-first flashcard domain models (not stored in Firestore yet).
 */
data class FlashcardCard(
    val id: String,
    val question: String,
    val keyword: String,
    val answer: String,
)

data class FlashcardDeck(
    val id: String,
    val title: String,
    val cards: List<FlashcardCard>,
)

enum class CardStudyStatus {
    UNSEEN,
    KNOWN,
    REPEAT,
}

data class FlashcardSessionResult(
    val sessionId: String,
    val deckId: String,
    val deckTitle: String,
    val totalCards: Int,
    val knownCount: Int,
    val reviewCount: Int,
    val studiedCount: Int,
    val durationMs: Long,
    val completedAt: Long = System.currentTimeMillis(),
) {
    val remainingCount: Int get() = (totalCards - studiedCount).coerceAtLeast(0)

    val accuracy: Float
        get() = if (studiedCount <= 0) 0f else knownCount.toFloat() / studiedCount

    fun formattedStudyTime(): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
