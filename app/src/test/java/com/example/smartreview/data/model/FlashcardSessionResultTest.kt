package com.example.smartreview.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FlashcardSessionResultTest {

    @Test
    fun accuracy_calculatesFromKnownOverStudied() {
        val session = FlashcardSessionResult(
            sessionId = "s1",
            deckId = "deck",
            deckTitle = "Deck",
            totalCards = 5,
            knownCount = 4,
            reviewCount = 1,
            studiedCount = 5,
            durationMs = 125_000,
        )
        assertEquals(0.8f, session.accuracy, 0.001f)
        assertEquals("02:05", session.formattedStudyTime())
    }
}
