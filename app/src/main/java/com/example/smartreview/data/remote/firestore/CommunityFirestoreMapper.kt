package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
import com.example.smartreview.data.model.MessageType
import com.example.smartreview.data.model.RoomIconType
import com.example.smartreview.data.model.withCurrentUserOwnership
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

object CommunityFirestoreMapper {

    // -----------------------------------------------------------------------
    // ChatRoom mapping
    // -----------------------------------------------------------------------

    fun toChatRoom(roomId: String, data: Map<String, Any>?): ChatRoom? {
        if (data == null) return null
        val dto = mapToRoomDocument(data)
        val name = resolveRoomName(roomId, dto)
        val lastMessageTime = dto.lastMessageAt as? Timestamp
        return ChatRoom(
            id = roomId,
            name = name,
            subject = dto.subject ?: "General",
            description = dto.description ?: "",
            lastMessage = dto.lastMessage ?: "",
            lastMessageTime = lastMessageTime,
            lastMessageUser = dto.lastMessageUser,
            isActive = dto.isActive ?: true,
            isLocked = dto.isLocked ?: false,
            memberCount = dto.memberCount ?: 0,
            messageCount = dto.messageCount ?: 0,
            isPinned = dto.pinned ?: false,
            roomType = dto.type ?: "general",
            createdBy = dto.createdBy ?: "",
            createdAt = dto.createdAt as? Timestamp,
            updatedAt = dto.updatedAt as? Timestamp,
            // UI-only fields
            unreadCount = 0,
            iconType = RoomIconType.FORUM,
            memberAvatars = emptyList(),
            isSystemRoom = false,
            isCurrentUserLast = false,
        )
    }

    // -----------------------------------------------------------------------
    // ChatMessage mapping (read from Firestore)
    // -----------------------------------------------------------------------

    fun toChatMessage(messageId: String, data: Map<String, Any>?): ChatMessage? {
        if (data == null) return null
        val dto = mapToMessageDocument(data)
        val text = dto.text.orEmpty()
        // Reject empty messages that carry no content and no file
        if (text.isBlank() && dto.fileUrl.isNullOrBlank()) return null

        val timestamp = dto.timestamp as? Timestamp ?: Timestamp.now()

        return ChatMessage(
            id = messageId,
            senderId = dto.userId.orEmpty(),       // Firestore field: "userId"
            senderName = dto.userName.orEmpty(),   // Firestore field: "userName"
            senderAvatar = "",                     // Not stored in Firestore schema
            content = text,                        // Firestore field: "text"
            timestamp = timestamp,                 // FIXED: Use Timestamp instead of String
            type = resolveMessageType(dto),
            fileUrl = dto.fileUrl,
            fileName = dto.fileName,
            fileType = dto.fileType,
            isImage = dto.isImage ?: false,
            isReported = dto.isReported ?: false,
            reportReason = dto.reportReason,
            reportedAt = dto.reportedAt as? Timestamp,
            reportedBy = dto.reportedBy,
            isCurrentUser = false,
        )
    }

    fun toChatMessage(
        messageId: String,
        data: Map<String, Any>?,
        currentUserId: String?,
    ): ChatMessage? = toChatMessage(messageId, data)?.withCurrentUserOwnership(currentUserId)

    // -----------------------------------------------------------------------
    // Sort key (for in-memory ordering when Firestore query has no orderBy)
    // -----------------------------------------------------------------------

    /** Returns epoch-millis from the "timestamp" field, or Long.MAX_VALUE if absent. */
    fun messageSortKey(data: Map<String, Any>?): Long {
        if (data == null) return Long.MAX_VALUE
        return mapToMessageDocument(data).let { dto ->
            timestampToMillis(dto.timestamp) ?: Long.MAX_VALUE
        }
    }

    // -----------------------------------------------------------------------
    // Write to Firestore — field names MUST match DA3-master chatService.ts
    // -----------------------------------------------------------------------

    /**
     * Builds the Firestore document map for a new message.
     *
     * Field names match the production schema and DA3-master/chatService.ts:
     *   userId, userName, text, timestamp, isReported
     *   fileUrl, fileName, fileType, isImage  (file messages only)
     */
    fun messageToFirestoreMap(message: ChatMessage): Map<String, Any> {
        val fileUrl = message.fileUrl
        val fields = mutableMapOf<String, Any>(
            "userId" to message.senderId,       // Firestore field: "userId"
            "userName" to message.senderName,   // Firestore field: "userName"
            "text" to message.content.trim(),   // Firestore field: "text"
            "timestamp" to FieldValue.serverTimestamp(), // Firestore field: "timestamp"
            "isReported" to false,
        )
        fileUrl?.takeIf { it.isNotBlank() }?.let { url ->
            fields["fileUrl"] = url
            fields["isImage"] = message.isImage || message.type == MessageType.IMAGE
            message.fileName?.takeIf { it.isNotBlank() }?.let { fields["fileName"] = it }
            message.fileType?.takeIf { it.isNotBlank() }?.let { fields["fileType"] = it }
        }
        return fields
    }

    fun roomUpdateAfterMessageMap(message: ChatMessage): Map<String, Any> =
        mapOf(
            "lastMessage" to lastMessagePreview(message),
            "lastMessageAt" to FieldValue.serverTimestamp(),
            "lastMessageUser" to message.senderName,
            "messageCount" to FieldValue.increment(1),
        )

    // -----------------------------------------------------------------------
    // Private helpers — document parsing
    // -----------------------------------------------------------------------

    private fun resolveRoomName(roomId: String, dto: ChatRoomDocument): String =
        dto.name?.takeIf { it.isNotBlank() } ?: roomId

    private fun lastMessagePreview(message: ChatMessage): String {
        val text = message.content.trim()
        if (text.isNotBlank()) return text.take(100)
        if (!message.fileUrl.isNullOrBlank()) {
            return if (message.isImage || message.type == MessageType.IMAGE) {
                "Image"
            } else {
                message.fileName?.takeIf { it.isNotBlank() }?.take(30) ?: "File"
            }
        }
        return ""
    }

    private fun resolveMessageType(dto: ChatMessageDocument): MessageType =
        if (dto.isImage == true || !dto.fileUrl.isNullOrBlank()) {
            MessageType.IMAGE
        } else {
            MessageType.TEXT
        }

    private fun mapToRoomDocument(data: Map<String, Any?>): ChatRoomDocument =
        ChatRoomDocument(
            name = stringField(data, "name"),
            description = stringField(data, "description"),
            type = stringField(data, "type"),
            subject = stringField(data, "subject"),
            createdBy = stringField(data, "createdBy"),
            createdAt = data["createdAt"],
            updatedAt = data["updatedAt"],
            lastMessage = stringField(data, "lastMessage"),
            lastMessageAt = data["lastMessageAt"],
            lastMessageUser = stringField(data, "lastMessageUser"),
            messageCount = numberField(data, "messageCount"),
            memberCount = numberField(data, "memberCount"),
            reportedCount = numberField(data, "reportedCount"),
            pinned = data["pinned"] as? Boolean,
            isActive = data["isActive"] as? Boolean,
            isLocked = data["isLocked"] as? Boolean,
        )

    private fun mapToMessageDocument(data: Map<String, Any?>): ChatMessageDocument =
        ChatMessageDocument(
            userId = stringField(data, "userId"),           // Firestore field: "userId"
            userName = stringField(data, "userName"),       // Firestore field: "userName"
            text = stringField(data, "text"),               // Firestore field: "text"
            timestamp = data["timestamp"],                  // Firestore Timestamp
            fileUrl = stringField(data, "fileUrl"),
            fileName = stringField(data, "fileName"),
            fileType = stringField(data, "fileType"),
            isImage = data["isImage"] as? Boolean,
            isReported = data["isReported"] as? Boolean,
            reportReason = stringField(data, "reportReason"),
            reportedBy = stringField(data, "reportedBy"),
            reportedAt = data["reportedAt"],
        )

    // -----------------------------------------------------------------------
    // Private helpers — field extraction
    // -----------------------------------------------------------------------

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

    /**
     * Safely converts a Firestore Timestamp or epoch Long to milliseconds.
     * Handles both Firestore SDK [Timestamp] objects and numeric epoch values.
     */
    private fun timestampToMillis(value: Any?): Long? = when (value) {
        is Timestamp -> value.toDate().time
        is Number -> value.toLong()
            else -> null
    }
}
