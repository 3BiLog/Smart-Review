package com.example.smartreview.data.repository

import com.example.smartreview.data.model.FlashcardDeck

/**
 * Local flashcard deck access (mock now; Firestore/Room later).
 */
interface FlashcardRepository {

    fun getDeck(deckId: String): FlashcardDeck?

    fun getDefaultDeck(): FlashcardDeck
}
