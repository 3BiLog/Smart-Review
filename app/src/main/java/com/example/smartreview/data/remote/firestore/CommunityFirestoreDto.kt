package com.example.smartreview.data.remote.firestore

data class ChatRoomDocument(
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val subject: String? = null,
    val createdBy: String? = null,
    val createdAt: Any? = null,
    val updatedAt: Any? = null,
    val lastMessage: String? = null,
    val lastMessageAt: Any? = null,
    val lastMessageUser: String? = null,
    val messageCount: Long? = null,
    val memberCount: Long? = null,
    val reportedCount: Long? = null,
    val pinned: Boolean? = null,
    val isActive: Boolean? = null,
    val isLocked: Boolean? = null,
)

data class ChatMessageDocument(
    val userId: String? = null,
    val userName: String? = null,
    val text: String? = null,
    val timestamp: Any? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileType: String? = null,
    val isImage: Boolean? = null,
    val isReported: Boolean? = null,
    val reportReason: String? = null,
    val reportedBy: String? = null,
    val reportedAt: Any? = null,
)
