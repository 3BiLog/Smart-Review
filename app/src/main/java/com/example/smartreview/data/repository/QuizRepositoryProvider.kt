package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.mock.MockQuizRepository

object QuizRepositoryProvider {
    val default: QuizRepository = MockQuizRepository()
}
