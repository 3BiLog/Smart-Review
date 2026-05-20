package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
import com.example.smartreview.data.model.MessageType
import com.example.smartreview.data.model.RoomIconType
import com.google.firebase.Timestamp

object CommunityFirestoreMapper {

    fun toChatRoom(roomId: String, data: Map<String, Any>?): ChatRoom? {
        if (data == null) return null
        val dto = mapToRoomDocument(data)
        val name = resolveRoomName(roomId, dto)
        return ChatRoom(
            id = roomId,
            name = name,
            lastMessage = dto.lastMessage.orEmpty(),
            lastMessageTime = dto.lastMessageTime.orEmpty(),
            isOnline = dto.isOnline ?: false,
            unreadCount = dto.unreadCount?.toInt() ?: 0,
            memberCount = dto.memberCount?.toInt() ?: 0,
            iconType = parseIconType(dto.iconType),
            memberAvatars = dto.memberAvatars.orEmpty(),
            isSystemRoom = dto.isSystemRoom ?: false,
            isCurrentUserLast = dto.isCurrentUserLast ?: false,
        )
    }

    fun toChatMessage(messageId: String, data: Map<String, Any>?): ChatMessage? {
        if (data == null) return null
        val dto = mapToMessageDocument(data)
        val type = parseMessageType(dto.type)
        val content = resolveMessageContent(dto, type)
        if (type != MessageType.DATE_SEPARATOR && content.isBlank() && dto.imageUrl.isNullOrBlank()) {
            return null
        }
        return ChatMessage(
            id = messageId,
            senderId = dto.senderId.orEmpty(),
            senderName = dto.senderName.orEmpty(),
            senderAvatar = dto.senderAvatar.orEmpty(),
            content = content,
            time = resolveMessageTime(dto),
            type = type,
            imageUrl = dto.imageUrl,
            isCurrentUser = dto.isCurrentUser ?: false,
        )
    }

    /** Sort key for in-memory ordering when Firestore query has no orderBy. */
    fun messageSortKey(data: Map<String, Any>?): Long {
        if (data == null) return Long.MAX_VALUE
        return mapToMessageDocument(data).createdAt ?: Long.MAX_VALUE
    }

    fun messageToFirestoreMap(message: ChatMessage, createdAt: Long = System.currentTimeMillis()): Map<String, Any> {
        val fields = mutableMapOf<String, Any>(
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "senderAvatar" to message.senderAvatar,
            "content" to message.content,
            "time" to message.time,
            "type" to message.type.name,
            "isCurrentUser" to message.isCurrentUser,
            "createdAt" to createdAt,
        )
        message.imageUrl?.takeIf { it.isNotBlank() }?.let { fields["imageUrl"] = it }
        return fields
    }

    private fun resolveRoomName(roomId: String, dto: ChatRoomDocument): String =
        dto.name?.takeIf { it.isNotBlank() } ?: roomId

    private fun resolveMessageContent(dto: ChatMessageDocument, type: MessageType): String {
        if (type == MessageType.DATE_SEPARATOR) return dto.content.orEmpty()
        return dto.content?.takeIf { it.isNotBlank() }.orEmpty()
    }

    private fun resolveMessageTime(dto: ChatMessageDocument): String {
        dto.time?.takeIf { it.isNotBlank() }?.let { return it }
        dto.createdAt?.let { return formatEpochMillis(it) }
        return ""
    }

    private fun formatEpochMillis(epochMs: Long): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        return "%d:%02d".format(hour, minute)
    }

    private fun mapToRoomDocument(data: Map<String, Any?>): ChatRoomDocument =
        ChatRoomDocument(
            name = stringField(data, "name", "title", "roomName"),
            lastMessage = stringField(data, "lastMessage", "last_message"),
            lastMessageTime = stringField(data, "lastMessageTime", "last_message_time"),
            isOnline = data["isOnline"] as? Boolean ?: data["is_online"] as? Boolean,
            unreadCount = numberField(data, "unreadCount", "unread_count"),
            memberCount = numberField(data, "memberCount", "member_count"),
            iconType = stringField(data, "iconType", "icon_type"),
            memberAvatars = listField(data, "memberAvatars", "member_avatars"),
            isSystemRoom = data["isSystemRoom"] as? Boolean ?: data["is_system_room"] as? Boolean,
            isCurrentUserLast = data["isCurrentUserLast"] as? Boolean ?: data["is_current_user_last"] as? Boolean,
        )

    private fun mapToMessageDocument(data: Map<String, Any?>): ChatMessageDocument =
        ChatMessageDocument(
            senderId = stringField(data, "senderId", "sender_id"),
            senderName = stringField(data, "senderName", "sender_name"),
            senderAvatar = stringField(data, "senderAvatar", "sender_avatar"),
            content = stringField(data, "content", "text", "message"),
            time = stringField(data, "time", "timestamp", "sentAt", "sent_at"),
            type = stringField(data, "type", "messageType", "message_type"),
            imageUrl = stringField(data, "imageUrl", "image_url"),
            isCurrentUser = data["isCurrentUser"] as? Boolean ?: data["is_current_user"] as? Boolean,
            createdAt = timestampField(data, "createdAt", "created_at", "timestamp"),
        )

    private fun stringField(data: Map<String, Any?>, vararg keys: String): String? {
        for (key in keys) {
            val value = data[key] as? String
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun numberField(data: Map<String, Any?>, vararg keys: String): Long? {
        for (key in keys) {
            when (val value = data[key]) {
                is Number -> return value.toLong()
            }
        }
        return null
    }

    private fun listField(data: Map<String, Any?>, vararg keys: String): List<String>? {
        for (key in keys) {
            val list = (data[key] as? List<*>)?.filterIsInstance<String>()
            if (!list.isNullOrEmpty()) return list
        }
        return null
    }

    private fun timestampField(data: Map<String, Any?>, vararg keys: String): Long? {
        for (key in keys) {
            when (val value = data[key]) {
                is Timestamp -> return value.toDate().time
                is Number -> return value.toLong()
            }
        }
        return null
    }

    private fun parseIconType(raw: String?): RoomIconType =
        runCatching { RoomIconType.valueOf(raw.orEmpty().uppercase()) }.getOrDefault(RoomIconType.FORUM)

    private fun parseMessageType(raw: String?): MessageType =
        runCatching { MessageType.valueOf(raw.orEmpty().uppercase()) }.getOrDefault(MessageType.TEXT)
}
