package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.mock.MockFlashcardData
import com.example.smartreview.data.model.FlashcardDeck
import com.example.smartreview.data.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MockFlashcardRepository : FlashcardRepository {

    override suspend fun getDeck(deckId: String): FlashcardDeck? = withContext(Dispatchers.IO) {
        MockFlashcardData.getDeck(deckId)
    }

    override suspend fun getDefaultDeck(): FlashcardDeck = withContext(Dispatchers.IO) {
        MockFlashcardData.defaultDeck
    }
}