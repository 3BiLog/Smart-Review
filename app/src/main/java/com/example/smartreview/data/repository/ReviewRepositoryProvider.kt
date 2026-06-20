package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreReviewRepository

object ReviewRepositoryProvider {
    val default: ReviewRepository = FirestoreReviewRepository()
}