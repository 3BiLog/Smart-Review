package com.example.smartreview.data.model

// ─── Chat Room ────────────────────────────────────────────────────────────────
data class ChatRoom(
    val id:              String,
    val name:            String,
    val lastMessage:     String,
    val lastMessageTime: String,
    val isOnline:        Boolean,
    val unreadCount:     Int            = 0,
    val memberCount:     Int,
    val iconType:        RoomIconType,
    val memberAvatars:   List<String>   = emptyList(),
    val isSystemRoom:    Boolean        = false,
    val isCurrentUserLast: Boolean      = false,  // "You: ..." prefix
)

enum class RoomIconType { SCHOOL, CODE, LANGUAGE, PSYCHOLOGY, ANNOUNCEMENT, FORUM, GLOBE }

// ─── Chat Message ─────────────────────────────────────────────────────────────
data class ChatMessage(
    val id:           String,
    val senderId:     String,
    val senderName:   String,
    val senderAvatar: String,
    val content:      String,
    val time:         String,
    val type:         MessageType,
    val imageUrl:     String?  = null,
    val isCurrentUser: Boolean = false,
)

enum class MessageType { TEXT, IMAGE, DATE_SEPARATOR }

// ─── Leaderboard ──────────────────────────────────────────────────────────────
data class LeaderboardEntry(
    val rank:          Int,
    val userId:        String,
    val displayName:   String,
    val avatarUrl:     String,
    val score:         Int,
    val progress:      Float,    // 0f–1f (relative to max score)
    val isCurrentUser: Boolean  = false,
)

enum class LeaderboardTab(val label: String) {
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
}