package com.example.smartreview.data.repository

import com.example.smartreview.data.model.UserProfile
import com.example.smartreview.data.repository.firestore.FirestoreUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// TEMPORARILY COMMENTED - Fix later
// import com.example.smartreview.data.repository.mock.MockUserRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 *
 * - [default] reads Firestore users/{uid}, falls back to [mock] on error.
 * - [mock] always uses local mock data (debug/tests).
 */
object UserRepositoryProvider {

    // TEMPORARILY COMMENTED - Mock is causing build errors
    // val mock: UserRepository = MockUserRepository()

    // Temporary fallback - use a simple object instead of mock
    private val emptyFallback = object : UserRepository {
        override suspend fun getCurrentUserProfile(): UserProfile? = null
        override suspend fun getUserProfile(uid: String): UserProfile? = null
        override fun observeCurrentUserProfile(): Flow<UserProfile?> = emptyFlow()
        override suspend fun updateCurrentUserProfile(displayName: String, phone: String): Boolean = false
        override suspend fun ensureUserProfileExists(uid: String, email: String, displayName: String?): UserProfile {
            return UserProfile(uid = uid, displayName = displayName ?: email, email = email)
        }
    }

    val default: UserRepository = FirestoreUserRepository(fallback = emptyFallback)
}