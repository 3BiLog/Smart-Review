package com.example.smartreview.data.repository

import com.example.smartreview.data.model.Quiz

interface QuizRepository {
    fun getQuiz(quizId: String): Quiz?
}
