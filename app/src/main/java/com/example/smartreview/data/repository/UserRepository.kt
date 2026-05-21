package com.example.smartreview.data.repository

import com.example.smartreview.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Data access contract for user profiles (Firestore users/{uid}).
 * ViewModels depend on this interface — not on Firebase APIs directly.
 */
interface UserRepository {

    /** Profile for the signed-in Firebase user, or null when not authenticated. */
    suspend fun getCurrentUserProfile(): UserProfile?

    /** Realtime profile for the signed-in user; emits null when logged out. */
    fun observeCurrentUserProfile(): Flow<UserProfile?>

    /** Profile for a specific uid; null when missing or on error. */
    suspend fun getUserProfile(uid: String): UserProfile?

    /**
     * Updates editable fields on users/{currentUid}. Returns false when unauthenticated or denied.
     */
    suspend fun updateCurrentUserProfile(displayName: String, phone: String): Boolean

    /**
     * Creates users/{uid} when missing (e.g. after registration).
     * Returns the existing or newly written profile.
     */
    suspend fun ensureUserProfileExists(
        uid: String,
        email: String,
        displayName: String? = null,
    ): UserProfile
}
