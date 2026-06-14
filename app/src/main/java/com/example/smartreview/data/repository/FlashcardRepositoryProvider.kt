package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreFlashcardRepository

object FlashcardRepositoryProvider {
    val default: FlashcardRepository = FirestoreFlashcardRepository()
}