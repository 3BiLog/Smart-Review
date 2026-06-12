package com.example.smartreview.data.remote.firestore

/**
 * Firestore document shapes for Community (field names match Firestore keys exactly).
 * Source of truth: FIRESTORE_SCHEMA_CURRENT.md + DA3-master/chatService.ts
 *
 * Nullable fields allow safe parsing when documents are partial or legacy.
 * UI-only fields (isOnline, unreadCount, iconType, etc.) are NOT stored in Firestore
 * and must be computed or defaulted in the mapper / domain layer.
 */

// ---------------------------------------------------------------------------
// chat_rooms/{roomId}
// ---------------------------------------------------------------------------
data class ChatRoomDocument(
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val subject: String? = null,
    val createdBy: String? = null,
    val createdAt: Any? = null,         // Firestore Timestamp
    val updatedAt: Any? = null,         // Firestore Timestamp
    val lastMessage: String? = null,
    val lastMessageAt: Any? = null,     // Firestore Timestamp
    val lastMessageUser: String? = null,
    val messageCount: Long? = null,
    val memberCount: Long? = null,
    val reportedCount: Long? = null,
    val pinned: Boolean? = null,
    val isActive: Boolean? = null,
    val isLocked: Boolean? = null,
)

// ---------------------------------------------------------------------------
// chat_rooms/{roomId}/messages/{messageId}
// ---------------------------------------------------------------------------
data class ChatMessageDocument(
    val userId: String? = null,         // was: senderId — matches Firestore "userId"
    val userName: String? = null,       // was: senderName — matches Firestore "userName"
    val text: String? = null,           // was: content — matches Firestore "text"
    val timestamp: Any? = null,         // Firestore Timestamp — matches Firestore "timestamp"
    val fileUrl: String? = null,        // matches Firestore "fileUrl"
    val fileName: String? = null,       // matches Firestore "fileName"
    val fileType: String? = null,       // matches Firestore "fileType"
    val isImage: Boolean? = null,       // matches Firestore "isImage"
    val isReported: Boolean? = null,    // matches Firestore "isReported"
    val reportReason: String? = null,   // matches Firestore "reportReason"
    val reportedBy: String? = null,     // matches Firestore "reportedBy"
    val reportedAt: Any? = null,        // Firestore Timestamp, matches Firestore "reportedAt"
)
