package com.example.smartreview.data.mock

import com.example.smartreview.data.model.FlashcardCard
import com.example.smartreview.data.model.FlashcardDeck

object MockFlashcardData {

    const val DEFAULT_DECK_ID = "ui_design"

    val defaultDeck = FlashcardDeck(
        id = DEFAULT_DECK_ID,
        title = "Flashcard: UI Design",
        cards = listOf(
            FlashcardCard(
                id = "fc_1",
                question = "What is Glassmorphism?",
                keyword = "Glassmorphism",
                answer = "A design style that uses frosted-glass effects — semi-transparent backgrounds with blur, subtle borders and soft shadows — to create depth and hierarchy in UI.",
            ),
            FlashcardCard(
                id = "fc_2",
                question = "What is a Design System?",
                keyword = "Design System",
                answer = "A collection of reusable components, guided by clear standards, that can be assembled together to build applications with consistent UX.",
            ),
            FlashcardCard(
                id = "fc_3",
                question = "Define UX Research.",
                keyword = "UX Research",
                answer = "The systematic study of target users and their requirements to add realistic context to design processes through interviews, surveys and usability tests.",
            ),
            FlashcardCard(
                id = "fc_4",
                question = "What is Visual Hierarchy?",
                keyword = "Visual Hierarchy",
                answer = "The arrangement of elements to show relative importance — guiding attention using size, color, contrast, spacing and placement.",
            ),
            FlashcardCard(
                id = "fc_5",
                question = "Explain Accessibility (a11y).",
                keyword = "Accessibility",
                answer = "Designing products usable by people with diverse abilities — including screen reader support, contrast, keyboard navigation and scalable text.",
            ),
        ),
    )

    private val decksById = mapOf(defaultDeck.id to defaultDeck)

    fun getDeck(deckId: String): FlashcardDeck? = decksById[deckId]
}
