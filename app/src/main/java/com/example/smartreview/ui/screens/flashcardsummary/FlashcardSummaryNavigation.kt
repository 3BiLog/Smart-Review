package com.example.smartreview.ui.screens.flashcardsummary

const val FLASHCARD_SUMMARY_ROUTE = "flashcard_summary/{sessionId}"

fun flashcardSummaryRoute(sessionId: String): String = "flashcard_summary/$sessionId"
