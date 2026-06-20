package com.example.smartreview.data.model

data class Flashcard(
    val id: String = "",
    val front: String = "",
    val back: String = "",
    val hint: String = ""
)

data class FlashcardDeck(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val cards: List<Flashcard> = emptyList(),
    val xpReward: Long = 30,
    val courseId: String? = null,
    val moduleId: String? = null,
    val lessonId: String? = null
)

@Deprecated("Use Flashcard with front/back instead")
data class FlashcardCard(
    val id: String,
    val question: String,
    val keyword: String,
    val answer: String,
)

enum class CardStudyStatus {
    UNSEEN,
    KNOWN,
    REPEAT,
    LEARNING
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
    val lessonId: String = "",
    val courseId: String = "",
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

fun Flashcard.toLegacyCard(index: Int): FlashcardCard = FlashcardCard(
    id = this.id,
    question = this.front,
    keyword = "Card ${index + 1}",
    answer = this.back
)