package com.example.smartreview.data.repository

import com.example.smartreview.data.model.UserProfile

/**
 * Data access contract for user profiles (Firestore users/{uid}).
 * ViewModels depend on this interface — not on Firebase APIs directly.
 */
interface UserRepository {

    /** Profile for the signed-in Firebase user, or null when not authenticated. */
    suspend fun getCurrentUserProfile(): UserProfile?

    /** Profile for a specific uid; null when missing or on error. */
    suspend fun getUserProfile(uid: String): UserProfile?

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
