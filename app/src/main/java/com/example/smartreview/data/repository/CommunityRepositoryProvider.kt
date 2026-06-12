package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreCommunityRepository
import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
// TEMPORARILY COMMENTED - Fix later when mock files are restored
// import com.example.smartreview.data.repository.mock.MockCommunityRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 *
 * - [default] uses Firestore when authenticated; mock fallback only on auth errors/offline.
 *   Guests receive empty data (no silent mock masking).
 * - [mock] always uses local mock data (debug/tests).
 * - [realtime] exposes Firestore snapshot listeners for a future ViewModel phase.
 */
object CommunityRepositoryProvider {

    // TEMPORARILY COMMENTED - Mock is causing build errors
    // val mock: CommunityRepository = MockCommunityRepository()

    // Temporary empty fallback
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