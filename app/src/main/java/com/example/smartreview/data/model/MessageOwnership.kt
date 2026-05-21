package com.example.smartreview.data.model

/**
 * Resolves whether a message belongs to the signed-in user.
 * [senderId] is the source of truth; [ChatMessage.isCurrentUser] is UI-only and session-derived.
 */
fun isMessageFromCurrentUser(senderId: String, currentUserId: String?): Boolean =
    !currentUserId.isNullOrBlank() && senderId == currentUserId

fun ChatMessage.withCurrentUserOwnership(currentUserId: String?): ChatMessage =
    copy(isCurrentUser = isMessageFromCurrentUser(senderId, currentUserId))

fun List<ChatMessage>.withCurrentUserOwnership(currentUserId: String?): List<ChatMessage> =
    map { it.withCurrentUserOwnership(currentUserId) }
