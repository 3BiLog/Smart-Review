package com.example.smartreview.data.repository

import com.example.smartreview.data.model.UserProfile
import com.example.smartreview.data.repository.firestore.FirestoreUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object UserRepositoryProvider {

    private val emptyFallback = object : UserRepository {
        override suspend fun getCurrentUserProfile(): UserProfile? = null
        override suspend fun getUserProfile(uid: String): UserProfile? = null
        override fun observeCurrentUserProfile(): Flow<UserProfile?> = emptyFlow()
        override suspend fun updateCurrentUserProfile(displayName: String, phone: String): Boolean = false
        override suspend fun ensureUserProfileExists(uid: String, email: String, displayName: String?): UserProfile {
            return UserProfile(uid = uid, displayName = displayName ?: email, email = email)
        }
        override suspend fun updateDailyGoal(dailyGoal: Long): Boolean = false
        override suspend fun addStudyTime(minutes: Long): Boolean = false
        override suspend fun resetDailyStudyTime(): Boolean = false
    }

    val default: UserRepository = FirestoreUserRepository(fallback = emptyFallback)
}