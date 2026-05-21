package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.mock.MockFlashcardRepository

object FlashcardRepositoryProvider {
    val default: FlashcardRepository = MockFlashcardRepository()
}
