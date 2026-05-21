package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.mock.MockFlashcardData
import com.example.smartreview.data.model.FlashcardDeck
import com.example.smartreview.data.repository.FlashcardRepository

class MockFlashcardRepository : FlashcardRepository {

    override fun getDeck(deckId: String): FlashcardDeck? = MockFlashcardData.getDeck(deckId)

    override fun getDefaultDeck(): FlashcardDeck = MockFlashcardData.defaultDeck
}
