package com.example.smartreview.data.repository

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
import kotlinx.coroutines.flow.Flow

interface CommunityRealtimeRepository {

    fun observeRooms(): Flow<List<ChatRoom>>

    fun observeSuggestedRooms(): Flow<List<ChatRoom>>

    fun observeMessages(roomId: String): Flow<List<ChatMessage>>

    suspend fun sendMessage(roomId: String, message: ChatMessage): String?

    suspend fun deleteMessage(roomId: String, messageId: String): Boolean
}
