package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
import com.example.smartreview.data.remote.firestore.CommunityFirestoreMapper
import com.example.smartreview.data.remote.firestore.CommunityFirestorePaths
import com.example.smartreview.data.repository.CommunityRealtimeRepository
import com.example.smartreview.data.repository.CommunityRepository
import com.example.smartreview.data.repository.mock.MockCommunityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firestore-backed Community repository with mock fallback for offline/empty/error cases.
 */
class FirestoreCommunityRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val fallback: CommunityRepository = MockCommunityRepository(),
) : CommunityRepository, CommunityRealtimeRepository {

    // ─── Sync reads (CommunityRepository) — used by current ViewModels ───────────

    override fun getRooms(): List<ChatRoom> = runBlocking(Dispatchers.IO) {
        fetchRoomsOrNull() ?: fallback.getRooms()
    }

    override fun getSuggestedRooms(): List<ChatRoom> = runBlocking(Dispatchers.IO) {
        fetchSuggestedRoomsOrNull() ?: fallback.getSuggestedRooms()
    }

    override fun getRoomName(roomId: String): String = runBlocking(Dispatchers.IO) {
        fetchRoomName(roomId) ?: fallback.getRoomName(roomId)
    }

    override fun getMessages(roomId: String): List<ChatMessage> = runBlocking(Dispatchers.IO) {
        fetchMessagesOrNull(roomId) ?: fallback.getMessages(roomId)
    }

    // ─── Realtime (CommunityRealtimeRepository) ───────────────────────────────────

    override fun observeRooms(): Flow<List<ChatRoom>> =
        collectionSnapshotFlow(CommunityFirestorePaths.ROOMS) { fallback.getRooms() }

    override fun observeSuggestedRooms(): Flow<List<ChatRoom>> =
        collectionSnapshotFlow(CommunityFirestorePaths.SUGGESTED_ROOMS) { fallback.getSuggestedRooms() }

    override fun observeMessages(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = messagesCollection(roomId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(fallback.getMessages(roomId))
                return@addSnapshotListener
            }
            val messages = mapAndSortMessages(snapshot?.documents.orEmpty())
            // Never replace a successful empty Firestore snapshot with mock data.
            trySend(messages)
        }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendMessage(roomId: String, message: ChatMessage): String? =
        withContext(Dispatchers.IO) {
            if (!isAuthenticated()) return@withContext null
            try {
                val ref = messagesCollection(roomId)
                    .add(CommunityFirestoreMapper.messageToFirestoreMap(message))
                    .await()
                ref.id
            } catch (_: Exception) {
                null
            }
        }

    // ─── Firestore helpers ───────────────────────────────────────────────────────

    private fun isAuthenticated(): Boolean = firebaseAuth.currentUser != null

    private suspend fun fetchRoomsOrNull(): List<ChatRoom>? =
        fetchCollection(CommunityFirestorePaths.ROOMS)

    private suspend fun fetchSuggestedRoomsOrNull(): List<ChatRoom>? =
        fetchCollection(CommunityFirestorePaths.SUGGESTED_ROOMS)

    /** Returns parsed rooms on success (may be empty). Null only on network/permission failure. */
    private suspend fun fetchCollection(collection: String): List<ChatRoom>? {
        if (!isAuthenticated()) return null
        return try {
            val snapshot = firestore.collection(collection).get().await()
            snapshot.documents.mapNotNull { doc ->
                CommunityFirestoreMapper.toChatRoom(doc.id, doc.data)
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchRoomName(roomId: String): String? {
        return try {
            if (!isAuthenticated()) return null
            val doc = firestore.collection(CommunityFirestorePaths.ROOMS).document(roomId).get().await()
            if (!doc.exists()) null
            else CommunityFirestoreMapper.toChatRoom(doc.id, doc.data)?.name
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Loads messages without requiring a Firestore composite index.
     * Tries ordered query when [createdAt] exists; otherwise falls back to plain get + in-memory sort.
     */
    private suspend fun fetchMessagesOrNull(roomId: String): List<ChatMessage>? {
        if (!isAuthenticated()) return null
        val collection = messagesCollection(roomId)
        val snapshot = try {
            collection.orderBy("createdAt", Query.Direction.ASCENDING).get().await()
        } catch (_: Exception) {
            try {
                collection.get().await()
            } catch (_: Exception) {
                return null
            }
        }
        return mapAndSortMessages(snapshot.documents)
    }

    private fun messagesCollection(roomId: String) =
        firestore
            .collection(CommunityFirestorePaths.ROOMS)
            .document(roomId)
            .collection(CommunityFirestorePaths.MESSAGES)

    private fun mapAndSortMessages(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>,
    ): List<ChatMessage> =
        documents
            .mapNotNull { doc ->
                CommunityFirestoreMapper.toChatMessage(doc.id, doc.data)?.let { msg ->
                    doc to msg
                }
            }
            .sortedBy { (doc, _) -> CommunityFirestoreMapper.messageSortKey(doc.data) }
            .map { (_, message) -> message }

    private fun collectionSnapshotFlow(
        collection: String,
        fallbackSupplier: () -> List<ChatRoom>,
    ): Flow<List<ChatRoom>> = callbackFlow {
        val registration = firestore.collection(collection).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(fallbackSupplier())
                return@addSnapshotListener
            }
            val rooms = snapshot?.documents
                ?.mapNotNull { doc -> CommunityFirestoreMapper.toChatRoom(doc.id, doc.data) }
                .orEmpty()
            trySend(rooms)
        }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)
}
