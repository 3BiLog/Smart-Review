package com.example.smartreview.data.remote.firestore

/**
 * Firestore document shapes for Community (field names match Firestore keys).
 * Nullable fields allow safe parsing when documents are partial or legacy.
 */
data class ChatRoomDocument(
    val name: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val isOnline: Boolean? = null,
    val unreadCount: Long? = null,
    val memberCount: Long? = null,
    val iconType: String? = null,
    val memberAvatars: List<String>? = null,
    val isSystemRoom: Boolean? = null,
    val isCurrentUserLast: Boolean? = null,
)

data class ChatMessageDocument(
    val senderId: String? = null,
    val senderName: String? = null,
    val senderAvatar: String? = null,
    val content: String? = null,
    val time: String? = null,
    val type: String? = null,
    val imageUrl: String? = null,
    val isCurrentUser: Boolean? = null,
    val createdAt: Long? = null,
)
