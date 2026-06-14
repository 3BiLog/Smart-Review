package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreQuizRepository

object QuizRepositoryProvider {
    val default: QuizRepository = FirestoreQuizRepository()
}