package com.example.smartreview.data.remote.firestore

/**
 * Firestore collection paths for the Community feature.
 *
 * Production Firestore structure (source of truth: DA3-master / FIRESTORE_SCHEMA_CURRENT.md):
 * - chat_rooms/{roomId}
 * - chat_rooms/{roomId}/messages/{messageId}
 *
 * Note: "suggested_rooms" does not exist in the production schema and has been removed.
 */
object CommunityFirestorePaths {
    const val ROOMS = "chat_rooms"
    const val MESSAGES = "messages"
}
