package com.example.smartreview.data.mock

import com.example.smartreview.data.model.Flashcard
import com.example.smartreview.data.model.FlashcardDeck

object MockFlashcardData {

    const val DEFAULT_DECK_ID = "ui_design"

    val defaultDeck = FlashcardDeck(
        id = DEFAULT_DECK_ID,
        title = "Flashcard: UI Design",
        cards = listOf(
            Flashcard(
                id = "fc_1",
                front = "What is Glassmorphism?",
                back = "A design style that uses frosted-glass effects — semi-transparent backgrounds with blur, subtle borders and soft shadows — to create depth and hierarchy in UI.",
                hint = "Frosted glass effect"
            ),
            Flashcard(
                id = "fc_2",
                front = "What is a Design System?",
                back = "A collection of reusable components, guided by clear standards, that can be assembled together to build applications with consistent UX.",
                hint = "Component library + standards"
            ),
            Flashcard(
                id = "fc_3",
                front = "Define UX Research.",
                back = "The systematic study of target users and their requirements to add realistic context to design processes.",
                hint = "User studies"
            ),
        ),
    )

    private val decksById = mapOf(defaultDeck.id to defaultDeck)

    fun getDeck(deckId: String): FlashcardDeck? = decksById[deckId]
}