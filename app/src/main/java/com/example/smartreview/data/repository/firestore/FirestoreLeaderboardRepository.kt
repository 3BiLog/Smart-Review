package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.remote.firestore.LeaderboardFirestoreMapper
import com.example.smartreview.data.remote.firestore.UserFirestoreMapper
import com.example.smartreview.data.remote.firestore.UserFirestorePaths
import com.example.smartreview.data.repository.LeaderboardRepository
import com.example.smartreview.data.repository.mock.MockLeaderboardRepository
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

/**
 * Realtime leaderboard from Firestore users collection (ordered by xp desc).
 * Guests receive empty data; mock fallback only when authenticated and query fails.
 */
class FirestoreLeaderboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val fallback: LeaderboardRepository = MockLeaderboardRepository(),
) : LeaderboardRepository {

    override fun getBaseEntries(): List<LeaderboardEntry> = runBlocking(Dispatchers.IO) {
        if (!isAuthenticated()) return@runBlocking emptyList()
        fetchLeaderboardOrNull() ?: fallback.getBaseEntries()
    }

    override fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        if (!isAuthenticated()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = leaderboardQuery().addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(fallback.getBaseEntries())
                return@addSnapshotListener
            }
            val profiles = snapshot?.documents
                ?.mapNotNull { doc -> UserFirestoreMapper.toUserProfile(doc.id, doc.data) }
                .orEmpty()
            trySend(LeaderboardFirestoreMapper.fromUserProfiles(profiles, currentUserId()))
        }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    private suspend fun fetchLeaderboardOrNull(): List<LeaderboardEntry>? = try {
        val snapshot = leaderboardQuery().get().await()
        val profiles = snapshot.documents
            .mapNotNull { doc -> UserFirestoreMapper.toUserProfile(doc.id, doc.data) }
        LeaderboardFirestoreMapper.fromUserProfiles(profiles, currentUserId())
    } catch (_: Exception) {
        null
    }

    private fun leaderboardQuery() =
        firestore.collection(UserFirestorePaths.USERS)
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(LeaderboardFirestoreMapper.LEADERBOARD_LIMIT)

    private fun isAuthenticated(): Boolean = firebaseAuth.currentUser != null

    private fun currentUserId(): String? = firebaseAuth.currentUser?.uid
}
