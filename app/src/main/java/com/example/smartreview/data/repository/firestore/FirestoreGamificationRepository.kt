package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.gamification.StreakCalculator
import com.example.smartreview.data.gamification.StudyDayFormatter
import com.example.smartreview.data.gamification.XpRewardAction
import com.example.smartreview.data.remote.firestore.GamificationFirestorePaths
import com.example.smartreview.data.remote.firestore.UserFirestoreMapper
import com.example.smartreview.data.remote.firestore.UserFirestorePaths
import com.example.smartreview.data.repository.GamificationRepository
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.mock.MockGamificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firestore transaction: idempotent reward ledger + atomic XP increment + streak/date sync.
 */
class FirestoreGamificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository,
    private val fallback: GamificationRepository = MockGamificationRepository(),
) : GamificationRepository {

    override suspend fun applyReward(
        uid: String,
        action: XpRewardAction,
        idempotencyKey: String,
    ): GamificationRewardResult = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid.isNullOrBlank() || currentUid != uid) {
            return@withContext GamificationRewardResult.NotAuthenticated
        }
        if (idempotencyKey.isBlank()) return@withContext GamificationRewardResult.Failed

        try {
            val email = firebaseAuth.currentUser?.email.orEmpty()
            if (email.isNotBlank()) {
                userRepository.ensureUserProfileExists(uid, email)
            }

            val todayKey = StudyDayFormatter.todayKey()
            val userRef = firestore.collection(UserFirestorePaths.USERS).document(uid)

            val outcome = firestore.runTransaction { transaction ->
                val ledgerRef = userRef
                    .collection(GamificationFirestorePaths.REWARD_LEDGER)
                    .document(sanitizeLedgerKey(idempotencyKey))

                if (transaction.get(ledgerRef).exists()) {
                    return@runTransaction LedgerOutcome.AlreadyProcessed
                }

                val userSnap = transaction.get(userRef)
                val profile = if (userSnap.exists()) {
                    UserFirestoreMapper.toUserProfile(userSnap.id, userSnap.data)
                } else {
                    null
                }

                val currentXp = profile?.xp ?: 0
                val currentStreak = profile?.streak ?: 0
                val lastStudyDate = profile?.lastStudyDate

                val streakUpdate = if (action.countsTowardStreak) {
                    StreakCalculator.computeStreak(currentStreak, lastStudyDate, todayKey)
                } else {
                    null
                }

                val userUpdates = mutableMapOf<String, Any>(
                    "xp" to FieldValue.increment(action.xpAmount.toLong()),
                )
                if (streakUpdate != null) {
                    userUpdates["streak"] = streakUpdate.newStreak
                    userUpdates["lastStudyDate"] = streakUpdate.todayStudyKey
                }

                transaction.update(userRef, userUpdates)
                transaction.set(
                    ledgerRef,
                    mapOf(
                        "action" to action.name,
                        "xpAwarded" to action.xpAmount,
                        "idempotencyKey" to idempotencyKey,
                        "processedAt" to FieldValue.serverTimestamp(),
                    ),
                )

                LedgerOutcome.Success(
                    xpAwarded = action.xpAmount,
                    newXp = currentXp + action.xpAmount,
                    newStreak = streakUpdate?.newStreak ?: currentStreak,
                    streakIncremented = streakUpdate?.streakIncremented ?: false,
                    todayStudyKey = streakUpdate?.todayStudyKey ?: lastStudyDate.orEmpty(),
                )
            }.await()

            when (outcome) {
                LedgerOutcome.AlreadyProcessed -> GamificationRewardResult.AlreadyProcessed()
                is LedgerOutcome.Success -> GamificationRewardResult.Success(
                    xpAwarded = outcome.xpAwarded,
                    newXp = outcome.newXp,
                    newStreak = outcome.newStreak,
                    streakIncremented = outcome.streakIncremented,
                    todayStudyKey = outcome.todayStudyKey,
                )
            }
        } catch (_: Exception) {
            fallback.applyReward(uid, action, idempotencyKey)
        }
    }

    private fun sanitizeLedgerKey(key: String): String =
        key.replace("/", "_").take(128)

    private sealed class LedgerOutcome {
        data object AlreadyProcessed : LedgerOutcome()
        data class Success(
            val xpAwarded: Int,
            val newXp: Int,
            val newStreak: Int,
            val streakIncremented: Boolean,
            val todayStudyKey: String,
        ) : LedgerOutcome()
    }
}
