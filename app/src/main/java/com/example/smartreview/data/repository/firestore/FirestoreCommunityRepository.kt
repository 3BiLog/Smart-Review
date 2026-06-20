package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.ChatMessage
import com.example.smartreview.data.model.ChatRoom
import com.example.smartreview.data.model.isMessageFromCurrentUser
import com.example.smartreview.data.model.withCurrentUserOwnership
import com.example.smartreview.data.remote.firestore.CommunityFirestoreMapper
import com.example.smartreview.data.remote.firestore.CommunityFirestorePaths
import com.example.smartreview.data.repository.CommunityRealtimeRepository
import com.example.smartreview.data.repository.CommunityRepository
// TEMPORARILY COMMENTED - Fix later when mock files are restored
// import com.example.smartreview.data.repository.mock.MockCommunityRepository
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

class FirestoreCommunityRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val fallback: CommunityRepository = EmptyCommunityFallback(),
) : CommunityRepository, CommunityRealtimeRepository {

    override fun getRooms(): List<ChatRoom> = runBlocking(Dispatchers.IO) {
        if (!isAuthenticated()) return@runBlocking emptyList()
        fetchRoomsOrNull() ?: fallback.getRooms()
    }


    override fun getSuggestedRooms(): List<ChatRoom> = runBlocking(Dispatchers.IO) {
        if (!isAuthenticated()) return@runBlocking emptyList()
        emptyList()
    }

    override fun getRoomName(roomId: String): String = runBlocking(Dispatchers.IO) {
        if (!isAuthenticated()) return@runBlocking fallback.getRoomName(roomId)
        fetchRoomName(roomId) ?: fallback.getRoomName(roomId)
    }

    override fun getMessages(roomId: String): List<ChatMessage> = runBlocking(Dispatchers.IO) {
        if (!isAuthenticated()) return@runBlocking emptyList()
        fetchMessagesOrNull(roomId) ?: fallback.getMessages(roomId)
    }

    override fun observeRooms(): Flow<List<ChatRoom>> =
        collectionSnapshotFlow(CommunityFirestorePaths.ROOMS) { fallback.getRooms() }

    override fun observeSuggestedRooms(): Flow<List<ChatRoom>> = callbackFlow {
        trySend(emptyList())
        awaitClose { }
    }

    override fun observeMessages(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (!isAuthenticated()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = messagesCollection(roomId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(fallback.getMessages(roomId).withCurrentUserOwnership(currentUserId()))
                return@addSnapshotListener
            }
            val messages = mapAndSortMessages(snapshot?.documents.orEmpty(), currentUserId())
            trySend(messages)
        }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun sendMessage(roomId: String, message: ChatMessage): String? =
        withContext(Dispatchers.IO) {
            if (!isAuthenticated()) return@withContext null
            try {
                val messageRef = messagesCollection(roomId).document()
                val roomRef = firestore
                    .collection(CommunityFirestorePaths.ROOMS)
                    .document(roomId)
                val batch = firestore.batch()
                batch.set(messageRef, CommunityFirestoreMapper.messageToFirestoreMap(message))
                batch.update(roomRef, CommunityFirestoreMapper.roomUpdateAfterMessageMap(message))
                batch.commit().await()
                messageRef.id
            } catch (_: Exception) {
                null
            }
        }

    override suspend fun deleteMessage(roomId: String, messageId: String): Boolean =
        withContext(Dispatchers.IO) {
            val uid = currentUserId()
            if (!isAuthenticated() || uid.isNullOrBlank() || messageId.isBlank()) return@withContext false
            try {
                val docRef = messagesCollection(roomId).document(messageId)
                val snapshot = docRef.get().await()
                if (!snapshot.exists()) return@withContext false
                val senderId = CommunityFirestoreMapper.toChatMessage(messageId, snapshot.data)?.senderId
                    .orEmpty()
                if (!isMessageFromCurrentUser(senderId, uid)) return@withContext false
                docRef.delete().await()
                true
            } catch (_: Exception) {
                false
            }
        }

    private fun isAuthenticated(): Boolean = firebaseAuth.currentUser != null

    private fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    private suspend fun fetchRoomsOrNull(): List<ChatRoom>? =
        fetchCollection(CommunityFirestorePaths.ROOMS)

    private suspend fun fetchCollection(collection: String): List<ChatRoom>? {
        if (!isAuthenticated()) return emptyList()
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

    private suspend fun fetchMessagesOrNull(roomId: String): List<ChatMessage>? {
        if (!isAuthenticated()) return emptyList()
        val collection = messagesCollection(roomId)
        val snapshot = try {
            collection.orderBy("timestamp", Query.Direction.ASCENDING).get().await()
        } catch (_: Exception) {
            try {
                collection.get().await()
            } catch (_: Exception) {
                return null
            }
        }
        return mapAndSortMessages(snapshot.documents, currentUserId())
    }

    private fun messagesCollection(roomId: String) =
        firestore
            .collection(CommunityFirestorePaths.ROOMS)
            .document(roomId)
            .collection(CommunityFirestorePaths.MESSAGES)

    private fun mapAndSortMessages(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>,
        currentUserId: String?,
    ): List<ChatMessage> =
        documents
            .mapNotNull { doc ->
                CommunityFirestoreMapper.toChatMessage(doc.id, doc.data, currentUserId)?.let { msg ->
                    doc to msg
                }
            }
            .sortedBy { (doc, _) -> CommunityFirestoreMapper.messageSortKey(doc.data) }
            .map { (_, message) -> message }

    private fun collectionSnapshotFlow(
        collection: String,
        fallbackSupplier: () -> List<ChatRoom>,
    ): Flow<List<ChatRoom>> = callbackFlow {
        if (!isAuthenticated()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
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

private class EmptyCommunityFallback : CommunityRepository {
    override fun getRooms(): List<ChatRoom> = emptyList()
    override fun getSuggestedRooms(): List<ChatRoom> = emptyList()
    override fun getRoomName(roomId: String): String = ""
    override fun getMessages(roomId: String): List<ChatMessage> = emptyList()
}
