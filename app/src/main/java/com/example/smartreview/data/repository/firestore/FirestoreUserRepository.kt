package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.UserProfile
import com.example.smartreview.data.remote.firestore.UserFirestoreMapper
import com.example.smartreview.data.remote.firestore.UserFirestorePaths
import com.example.smartreview.data.repository.UserRepository
// TEMPORARILY COMMENTED - Fix later when mock files are restored
// import com.example.smartreview.data.repository.mock.MockUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp

/**
 * Firestore-backed user profiles aligned with production users/{uid} schema.
 *
 * Domain mapping (UserFirestoreMapper):
 * - name -> displayName
 * - totalXP (fallback xp) -> xp
 * - currentStreak -> streak
 * - createdAt -> joinedAt
 * - lastStreakDate -> lastStreakDate
 */
class FirestoreUserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    // TEMPORARILY CHANGED: Use empty fallback instead of Mock
    private val fallback: UserRepository = EmptyUserFallback(),
) : UserRepository {

    override suspend fun getCurrentUserProfile(): UserProfile? = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext null
        fetchUserProfile(uid) ?: run {
            val email = firebaseAuth.currentUser?.email.orEmpty()
            if (email.isBlank()) fallback.getCurrentUserProfile()
            else ensureUserProfileExists(uid, email)
        }
    }

    override suspend fun getUserProfile(uid: String): UserProfile? = withContext(Dispatchers.IO) {
        fetchUserProfile(uid)
    }

    override fun observeCurrentUserProfile(): Flow<UserProfile?> = callbackFlow {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val registration = userDocument(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            val profile = if (snapshot?.exists() == true) {
                UserFirestoreMapper.toUserProfile(snapshot.id, snapshot.data)
            } else {
                null
            }
            trySend(profile)
        }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateCurrentUserProfile(
        displayName: String,
        phone: String,
    ): Boolean = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext false
        val trimmedName = displayName.trim()
        if (trimmedName.isBlank()) return@withContext false
        try {
            val updates = UserFirestoreMapper.profileUpdateMap(trimmedName, phone)
            userDocument(uid)
                .set(updates, SetOptions.merge())
                .await()
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun ensureUserProfileExists(
        uid: String,
        email: String,
        displayName: String?,
    ): UserProfile = withContext(Dispatchers.IO) {
        val existing = fetchUserProfile(uid)
        if (existing != null) return@withContext existing

        val docRef = userDocument(uid)
        val payload = UserFirestoreMapper.newUserFirestoreMap(uid, email, displayName)

        try {
            docRef.set(payload, SetOptions.merge()).await()
            fetchUserProfile(uid) ?: buildProfileFromAuth(uid, email, displayName)
        } catch (_: Exception) {
            fallback.ensureUserProfileExists(uid, email, displayName)
        }
    }

    private suspend fun fetchUserProfile(uid: String): UserProfile? = try {
        val snapshot = userDocument(uid).get().await()
        if (!snapshot.exists()) null
        else UserFirestoreMapper.toUserProfile(snapshot.id, snapshot.data)
    } catch (_: Exception) {
        null
    }

    private fun userDocument(uid: String) =
        firestore.collection(UserFirestorePaths.USERS).document(uid)

    private fun buildProfileFromAuth(
        uid: String,
        email: String,
        displayName: String?,
    ): UserProfile = UserProfile(
        uid = uid,
        displayName = displayName?.takeIf { it.isNotBlank() }
            ?: UserFirestoreMapper.defaultDisplayName(email, uid),
        email = email.trim(),
        avatarUrl = UserFirestoreMapper.defaultAvatarUrl(uid),
        phone = "",
        streak = 0,
        xp = 0,
        lastStreakDate = null,
        joinedAt = Timestamp.now(),
        longestStreak = 0,
        role = "user",
        status = "active",
        warningCount = 0,
        lastLogin = Timestamp.now(),
        updatedAt = Timestamp.now(),
        bannedAt = null,
        bannedUntil = null,
        bannedReason = null
    )
}

/**
 * Empty fallback implementation for when no network or not authenticated.
 */
private class EmptyUserFallback : UserRepository {
    override suspend fun getCurrentUserProfile(): UserProfile? = null
    override suspend fun getUserProfile(uid: String): UserProfile? = null
    override fun observeCurrentUserProfile(): Flow<UserProfile?> = callbackFlow {
        trySend(null)
        awaitClose { }
    }
    override suspend fun updateCurrentUserProfile(displayName: String, phone: String): Boolean = false
    override suspend fun ensureUserProfileExists(uid: String, email: String, displayName: String?): UserProfile {
        return UserProfile(
            uid = uid,
            displayName = displayName ?: email.substringBefore("@"),
            email = email
        )
    }
}
