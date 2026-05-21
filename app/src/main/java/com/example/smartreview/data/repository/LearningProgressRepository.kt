package com.example.smartreview.data.repository

import com.example.smartreview.data.model.UserLearningProgress

/**
 * Persists [UserLearningProgress] per Firebase uid (SharedPreferences now; Room later).
 */
interface LearningProgressRepository {

    suspend fun load(uid: String): UserLearningProgress?

    suspend fun save(progress: UserLearningProgress)

    suspend fun clear(uid: String)
}
