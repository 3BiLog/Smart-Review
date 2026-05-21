package com.example.smartreview.data.remote.firestore

/**
 * Firestore document shape for users/{uid}.
 */
data class UserDocument(
    val uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val streak: Long? = null,
    val xp: Long? = null,
    val lastStudyDate: String? = null,
    val joinedAt: Long? = null,
)
