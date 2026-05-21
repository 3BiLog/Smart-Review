package com.example.smartreview.data.remote.firestore

import com.example.smartreview.data.model.MessageType
import com.example.smartreview.data.model.RoomIconType
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Verifies mapping against the real Firestore layout:
 * rooms/room1|room2|room3 + rooms/{id}/messages/{messageId}
 */
class CommunityFirestoreMapperTest {

    @Test
    fun toChatRoom_usesDocumentIdWhenNameMissing() {
        val room = CommunityFirestoreMapper.toChatRoom("room1", emptyMap())
        assertNotNull(room)
        assertEquals("room1", room!!.id)
        assertEquals("room1", room.name)
    }

    @Test
    fun toChatRoom_mapsCanonicalFields() {
        val room = CommunityFirestoreMapper.toChatRoom(
            "room1",
            mapOf(
                "name" to "Luyện thi TOEIC",
                "lastMessage" to "Hello",
                "lastMessageTime" to "10:00",
                "isOnline" to true,
                "memberCount" to 15,
                "iconType" to "SCHOOL",
            ),
        )
        assertEquals("Luyện thi TOEIC", room?.name)
        assertEquals(RoomIconType.SCHOOL, room?.iconType)
        assertEquals(15, room?.memberCount)
    }

    @Test
    fun toChatRoom_mapsTitleAlias() {
        val room = CommunityFirestoreMapper.toChatRoom(
            "room2",
            mapOf("title" to "Góc học tập"),
        )
        assertEquals("Góc học tập", room?.name)
    }

    @Test
    fun toChatMessage_mapsCanonicalFields() {
        val createdAt = 1_700_000_000_000L
        val message = CommunityFirestoreMapper.toChatMessage(
            "msg1",
            mapOf(
                "senderId" to "u1",
                "senderName" to "Minh",
                "senderAvatar" to "https://example.com/a.png",
                "content" to "Xin chào",
                "time" to "Now",
                "type" to "TEXT",
                "createdAt" to createdAt,
            ),
        )
        assertNotNull(message)
        assertEquals("Minh", message!!.senderName)
        assertEquals("Xin chào", message.content)
        assertEquals(MessageType.TEXT, message.type)
        assertTrue(message.time.isNotBlank())
        assertTrue(!message.time.equals("Now", ignoreCase = true))
    }

    @Test
    fun toChatMessage_mapsTextAlias() {
        val message = CommunityFirestoreMapper.toChatMessage(
            "msg2",
            mapOf(
                "text" to "Firestore message",
                "senderName" to "Lan",
            ),
        )
        assertEquals("Firestore message", message?.content)
    }

    @Test
    fun toChatMessage_returnsNullWhenNoContent() {
        val message = CommunityFirestoreMapper.toChatMessage("msg3", mapOf("senderName" to "Lan"))
        assertNull(message)
    }

    @Test
    fun toChatMessage_ignoresPersistedIsCurrentUser() {
        val message = CommunityFirestoreMapper.toChatMessage(
            "msg_owner",
            mapOf(
                "senderId" to "user_a",
                "senderName" to "User A",
                "content" to "Hello",
                "isCurrentUser" to true,
            ),
            currentUserId = "user_b",
        )
        assertEquals(false, message!!.isCurrentUser)
    }

    @Test
    fun toChatMessage_resolvesOwnershipFromSenderId() {
        val message = CommunityFirestoreMapper.toChatMessage(
            "msg_mine",
            mapOf(
                "senderId" to "user_a",
                "senderName" to "User A",
                "content" to "My message",
            ),
            currentUserId = "user_a",
        )
        assertEquals(true, message!!.isCurrentUser)
    }

    @Test
    fun messageToFirestoreMap_doesNotPersistIsCurrentUser() {
        val createdAt = 1_700_000_000_000L
        val map = CommunityFirestoreMapper.messageToFirestoreMap(
            com.example.smartreview.data.model.ChatMessage(
                id = "1",
                senderId = "uid_1",
                senderName = "A",
                senderAvatar = "https://example.com/a.png",
                content = "Hi",
                time = "Now",
                type = MessageType.TEXT,
                isCurrentUser = true,
            ),
            createdAt = createdAt,
        )
        assertEquals(false, map.containsKey("isCurrentUser"))
        assertEquals(createdAt, map["createdAt"])
        assertTrue((map["time"] as String).isNotBlank())
        assertTrue(!(map["time"] as String).equals("Now", ignoreCase = true))
    }

    @Test
    fun messageSortKey_supportsFirestoreTimestamp() {
        val ts = Timestamp(Date(1_700_000_000_000L))
        val key = CommunityFirestoreMapper.messageSortKey(mapOf("createdAt" to ts))
        assertEquals(1_700_000_000_000L, key)
    }
}
