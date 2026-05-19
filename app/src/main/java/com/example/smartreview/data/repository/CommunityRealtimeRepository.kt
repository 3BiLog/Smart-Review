package com.example.smartreview.data.repository

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
import kotlinx.coroutines.flow.Flow

/**
 * Realtime Community data contract (Firestore snapshot listeners).
 * ViewModels can adopt this in a later phase without changing [CommunityRepository].
 */
interface CommunityRealtimeRepository {

    fun observeRooms(): Flow<List<ChatRoom>>

    fun observeSuggestedRooms(): Flow<List<ChatRoom>>

    fun observeMessages(roomId: String): Flow<List<ChatMessage>>

    suspend fun sendMessage(roomId: String, message: ChatMessage): Boolean
}
