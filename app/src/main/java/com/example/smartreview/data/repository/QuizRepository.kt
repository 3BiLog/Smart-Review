package com.example.smartreview.data.repository

import com.example.smartreview.data.model.Quiz

interface QuizRepository {
    suspend fun getQuiz(quizId: String): Quiz?
}