package com.example.smartreview.data.model

fun isMessageFromCurrentUser(senderId: String, currentUserId: String?): Boolean =
    !currentUserId.isNullOrBlank() && senderId == currentUserId

fun ChatMessage.withCurrentUserOwnership(currentUserId: String?): ChatMessage =
    copy(isCurrentUser = isMessageFromCurrentUser(senderId, currentUserId))

fun List<ChatMessage>.withCurrentUserOwnership(currentUserId: String?): List<ChatMessage> =
    map { it.withCurrentUserOwnership(currentUserId) }
