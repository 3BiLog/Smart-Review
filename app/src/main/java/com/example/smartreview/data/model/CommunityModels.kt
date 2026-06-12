package com.example.smartreview.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.*

// ─── Chat Room ────────────────────────────────────────────────────────────────
data class ChatRoom(
    val id: String,
    val name: String,
    val subject: String = "",
    val description: String = "",

    // FIXED: Use proper types matching Web Admin schema
    @get:PropertyName("lastMessage")
    val lastMessage: String? = null,

    @get:PropertyName("lastMessageAt")
    val lastMessageTime: Timestamp? = null,

    @get:PropertyName("lastMessageUser")
    val lastMessageUser: String? = null,

    @get:PropertyName("isActive")
    val isActive: Boolean = true,

    @get:PropertyName("isLocked")
    val isLocked: Boolean = false,

    @get:PropertyName("memberCount")
    val memberCount: Long = 0,

    @get:PropertyName("messageCount")
    val messageCount: Long = 0,

    @get:PropertyName("pinned")
    val isPinned: Boolean = false,

    @get:PropertyName("type")
    val roomType: String = "general",  // "general", "course", "announcement"

    @get:PropertyName("createdBy")
    val createdBy: String = "",

    @get:PropertyName("createdAt")
    val createdAt: Timestamp? = null,

    @get:PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,

    // UI-specific fields (not stored in Firestore)
    val unreadCount: Int = 0,
    val iconType: RoomIconType = RoomIconType.FORUM,
    val memberAvatars: List<String> = emptyList(),
    val isSystemRoom: Boolean = false,
    val isCurrentUserLast: Boolean = false
) {
    // Helper to format last message time for UI
    fun getFormattedLastMessageTime(): String {
        return lastMessageTime?.toDate()?.let { date ->
            val now = Date()
            val calendar = Calendar.getInstance()
            calendar.time = date

            when {
                isToday(date) -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                isThisWeek(date) -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
            }
        } ?: ""
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    private fun isThisWeek(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        val weekStart = calendar.apply { set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek) }.time
        return date.after(weekStart)
    }
}

enum class RoomIconType { SCHOOL, CODE, LANGUAGE, PSYCHOLOGY, ANNOUNCEMENT, FORUM, GLOBE }

// ─── Chat Message ─────────────────────────────────────────────────────────────
data class ChatMessage(
    val id: String,

    // FIXED: "senderId" -> "userId" to match Web Admin schema
    @get:PropertyName("userId")
    val senderId: String,

    // FIXED: "senderName" -> "userName"
    @get:PropertyName("userName")
    val senderName: String,

    // FIXED: "content" -> "text"
    @get:PropertyName("text")
    val content: String,

    // FIXED: String -> Timestamp, "time" -> "timestamp"
    @get:PropertyName("timestamp")
    val timestamp: Timestamp,

    // NEW: File attachment fields from Web Admin schema
    @get:PropertyName("fileUrl")
    val fileUrl: String? = null,

    @get:PropertyName("fileName")
    val fileName: String? = null,

    @get:PropertyName("fileType")
    val fileType: String? = null,  // "image", "pdf", "document"

    @get:PropertyName("isImage")
    val isImage: Boolean = false,

    // NEW: Moderation fields
    @get:PropertyName("isReported")
    val isReported: Boolean = false,

    @get:PropertyName("reportReason")
    val reportReason: String? = null,

    @get:PropertyName("reportedAt")
    val reportedAt: Timestamp? = null,

    @get:PropertyName("reportedBy")
    val reportedBy: String? = null,

    // UI-specific fields (not stored in Firestore)
    val senderAvatar: String = "",
    val type: MessageType = MessageType.TEXT,
    val isCurrentUser: Boolean = false
) {
    // Helper to format timestamp for UI display
    fun getFormattedTime(): String {
        val date = timestamp.toDate()
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date

        return when {
            isToday(date) -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(date)
        }
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
}

enum class MessageType { TEXT, IMAGE, DATE_SEPARATOR }

// ─── Leaderboard ──────────────────────────────────────────────────────────────
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String,
    // FIXED: Use Long to match Firestore totalXP field
    val score: Long,
    val progress: Float,  // 0f–1f (relative to max score)
    val isCurrentUser: Boolean = false
)

enum class LeaderboardTab(val label: String) {
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này"),
}

// ─── Helper extension to convert from old format to new format ─────────────────
fun ChatMessage.toMapForFirestore(): Map<String, Any?> {
    return mapOf(
        "userId" to senderId,
        "userName" to senderName,
        "text" to content,
        "timestamp" to timestamp,
        "fileUrl" to fileUrl,
        "fileName" to fileName,
        "fileType" to fileType,
        "isImage" to isImage,
        "isReported" to isReported,
        "reportReason" to reportReason,
        "reportedAt" to reportedAt,
        "reportedBy" to reportedBy
    )
}