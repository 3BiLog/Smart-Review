package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreCommunityRepository
import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom

object CommunityRepositoryProvider {

    private val emptyFallback = object : CommunityRepository {
        override fun getRooms(): List<ChatRoom> = emptyList()
        override fun getSuggestedRooms(): List<ChatRoom> = emptyList()
        override fun getRoomName(roomId: String): String = ""
        override fun getMessages(roomId: String): List<ChatMessage> = emptyList()
    }

    private val firestoreRepository = FirestoreCommunityRepository(fallback = emptyFallback)

    val default: CommunityRepository = firestoreRepository

    val realtime: CommunityRealtimeRepository = firestoreRepository
}