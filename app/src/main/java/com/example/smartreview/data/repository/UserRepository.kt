package com.example.smartreview.data.repository

import com.example.smartreview.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun getCurrentUserProfile(): UserProfile?

    fun observeCurrentUserProfile(): Flow<UserProfile?>

    suspend fun getUserProfile(uid: String): UserProfile?

    suspend fun updateCurrentUserProfile(displayName: String, phone: String): Boolean

    suspend fun ensureUserProfileExists(
        uid: String,
        email: String,
        displayName: String? = null,
    ): UserProfile

    suspend fun updateDailyGoal(dailyGoal: Long): Boolean

    suspend fun addStudyTime(minutes: Long): Boolean

    suspend fun resetDailyStudyTime(): Boolean
}
