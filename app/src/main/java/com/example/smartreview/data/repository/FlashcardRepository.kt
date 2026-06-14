package com.example.smartreview.data.repository

import com.example.smartreview.data.model.FlashcardDeck

/**
 * Flashcard deck access - now reads from Firestore
 */
interface FlashcardRepository {
    suspend fun getDeck(deckId: String): FlashcardDeck?
    suspend fun getDefaultDeck(): FlashcardDeck
}