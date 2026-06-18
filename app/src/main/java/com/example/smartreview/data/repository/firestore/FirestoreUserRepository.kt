package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.UserProfile
import com.example.smartreview.data.remote.firestore.UserFirestoreMapper
import com.example.smartreview.data.remote.firestore.UserFirestorePaths
import com.example.smartreview.data.repository.UserRepository
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
import java.util.Calendar

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

    // ✅ NEW: Update daily goal
    override suspend fun updateDailyGoal(dailyGoal: Long): Boolean = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext false
        try {
            val updates = UserFirestoreMapper.updateDailyGoalMap(dailyGoal)
            userDocument(uid).set(updates, SetOptions.merge()).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ✅ NEW: Add study time and check for goal completion
    override suspend fun addStudyTime(minutes: Long): Boolean = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext false

        try {
            // Get current profile
            val profile = fetchUserProfile(uid) ?: return@withContext false

            // Check if need to reset daily (new day)
            val shouldReset = shouldResetDailyStudyTime(profile)

            val currentStudyTime = if (shouldReset) 0L else profile.todayStudyTime
            val newStudyTime = currentStudyTime + minutes

            // Check if goal is completed
            val goal = profile.dailyGoal
            val wasCompleted = currentStudyTime >= goal
            val isNowCompleted = newStudyTime >= goal

            val goalJustCompleted = !wasCompleted && isNowCompleted
            val xpReward = if (goalJustCompleted) calculateDailyGoalXP(goal) else 0L
            val xpEarned = if (goalJustCompleted) xpReward else profile.dailyGoalXP

            val updates = mutableMapOf<String, Any>(
                UserFirestorePaths.Fields.UPDATED_AT to Timestamp.now(),
            )

            if (shouldReset) {
                updates[UserFirestorePaths.Fields.TODAY_STUDY_TIME] = minutes
                updates[UserFirestorePaths.Fields.LAST_RESET_DATE] = Timestamp.now()
                updates[UserFirestorePaths.Fields.DAILY_GOAL_XP] =
                    if (isNowCompleted) calculateDailyGoalXP(goal) else 0L
            } else {
                updates[UserFirestorePaths.Fields.TODAY_STUDY_TIME] = newStudyTime
                updates[UserFirestorePaths.Fields.DAILY_GOAL_XP] = xpEarned
            }

            if (goalJustCompleted) {
                val newTotalXp = profile.xp + xpReward
                updates[UserFirestorePaths.Fields.TOTAL_XP] = newTotalXp
                updates[UserFirestorePaths.Fields.XP] = newTotalXp
            }

            userDocument(uid).set(updates, SetOptions.merge()).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ✅ NEW: Reset daily study time
    override suspend fun resetDailyStudyTime(): Boolean = withContext(Dispatchers.IO) {
        val uid = firebaseAuth.currentUser?.uid ?: return@withContext false
        try {
            val updates = UserFirestoreMapper.resetTodayStudyTimeMap()
            userDocument(uid).set(updates, SetOptions.merge()).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    // Helper: Check if daily study time should be reset
    private fun shouldResetDailyStudyTime(profile: UserProfile): Boolean {
        val lastReset = profile.lastResetDate?.toDate() ?: return true
        val today = getTodayDate()
        val lastResetDate = getDateWithoutTime(lastReset)
        return today != lastResetDate
    }

    private fun getTodayDate(): String = formatDateKey(Calendar.getInstance())

    private fun getDateWithoutTime(date: java.util.Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return formatDateKey(calendar)
    }

    private fun formatDateKey(calendar: Calendar): String {
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "${calendar.get(Calendar.YEAR)}-$month-$day"
    }

    // Calculate XP based on daily goal
    private fun calculateDailyGoalXP(goalMinutes: Long): Long {
        return when (goalMinutes) {
            15L -> 10L
            30L -> 25L
            60L -> 50L
            else -> 20L
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
        bannedReason = null,
        // ✅ NEW: Default values for daily goal fields
        dailyGoal = 30,
        todayStudyTime = 0,
        lastResetDate = Timestamp.now(),
        dailyGoalXP = 0,
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
            email = email,
            // ✅ NEW: Default values
            dailyGoal = 30,
            todayStudyTime = 0,
            lastResetDate = Timestamp.now(),
            dailyGoalXP = 0,
        )
    }

    // ✅ NEW: Implement new functions
    override suspend fun updateDailyGoal(dailyGoal: Long): Boolean = false
    override suspend fun addStudyTime(minutes: Long): Boolean = false
    override suspend fun resetDailyStudyTime(): Boolean = false
}