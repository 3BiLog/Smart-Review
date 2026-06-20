package com.example.smartreview.data.repository

import com.example.smartreview.data.model.UserLearningProgress

interface LearningProgressRepository {

    suspend fun load(uid: String): UserLearningProgress?

    suspend fun save(progress: UserLearningProgress)

    suspend fun clear(uid: String)
}
