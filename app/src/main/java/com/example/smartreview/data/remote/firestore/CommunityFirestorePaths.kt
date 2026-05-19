package com.example.smartreview.data.remote.firestore

/**
 * Firestore collection paths for the Community feature.
 *
 * Structure:
 * - rooms/{roomId}
 * - rooms/{roomId}/messages/{messageId}
 * - suggested_rooms/{roomId}
 */
object CommunityFirestorePaths {
    const val ROOMS = "rooms"
    const val SUGGESTED_ROOMS = "suggested_rooms"
    const val MESSAGES = "messages"
}
