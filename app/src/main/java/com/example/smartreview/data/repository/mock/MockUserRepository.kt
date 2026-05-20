package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.mock.MockUserData
import com.example.smartreview.data.model.UserProfile
import com.example.smartreview.data.repository.UserRepository

/**
 * Local mock profiles for offline/debug and Firestore fallback.
 */
class MockUserRepository : UserRepository {

    override suspend fun getCurrentUserProfile(): UserProfile = MockUserData.defaultProfile

    override suspend fun getUserProfile(uid: String): UserProfile? =
        if (uid == MockUserData.defaultProfile.uid) MockUserData.defaultProfile else null

    override suspend fun ensureUserProfileExists(
        uid: String,
        email: String,
        displayName: String?,
    ): UserProfile = MockUserData.defaultProfile.copy(
        uid = uid,
        email = email,
        displayName = displayName?.takeIf { it.isNotBlank() } ?: MockUserData.defaultProfile.displayName,
    )
}
