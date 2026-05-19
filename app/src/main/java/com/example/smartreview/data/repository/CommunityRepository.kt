package com.example.smartreview.data.repository

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom

/**
 * Data access contract for the Community feature (rooms + chat).
 * ViewModels depend on this interface — not on inline mock data.
 */
interface CommunityRepository {

    fun getRooms(): List<ChatRoom>

    fun getSuggestedRooms(): List<ChatRoom>

    fun getRoomName(roomId: String): String

    fun getMessages(roomId: String): List<ChatMessage>
}
