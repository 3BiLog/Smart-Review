package com.example.smartreview.data.repository.firestore

import android.util.Log
import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.gamification.StreakCalculator
import com.example.smartreview.data.gamification.StudyDayFormatter
import com.example.smartreview.data.gamification.XpRewardAction
import com.example.smartreview.data.remote.firestore.GamificationFirestorePaths
import com.example.smartreview.data.remote.firestore.UserFirestoreMapper
import com.example.smartreview.data.remote.firestore.UserFirestorePaths
import com.example.smartreview.data.repository.GamificationRepository
import com.example.smartreview.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firestore transaction: idempotent reward ledger + atomic XP increment + streak/date sync.
 *
 * Security rules must allow the signed-in user to write `users/{uid}` fields:
 * `xp`, `streak`, `lastStudyDate`, and subcollection `rewardLedger/{ledgerId}`.
 */
class FirestoreGamificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository,
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
        if (idempotencyKey.isBlank()) {
            return@withContext GamificationRewardResult.Failed("idempotency_key_blank")
        }

        val email = firebaseAuth.currentUser?.email.orEmpty()
        try {
            if (email.isNotBlank()) {
                userRepository.ensureUserProfileExists(uid, email)
            }
            ensureUserDocumentExists(uid, email)
        } catch (e: Exception) {
            logRewardFailure(uid, idempotencyKey, action, "ensure_user_profile", e)
            return@withContext GamificationRewardResult.Failed(failureReason(e))
        }

        val todayKey = StudyDayFormatter.todayKey()
        val userRef = firestore.collection(UserFirestorePaths.USERS).document(uid)

        try {
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

                // set(merge) works when the user doc was just created or already exists (update() fails if missing).
                transaction.set(userRef, userUpdates, SetOptions.merge())
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
                LedgerOutcome.AlreadyProcessed -> {
                    Log.i(TAG, "reward_already_processed uid=$uid key=$idempotencyKey")
                    GamificationRewardResult.AlreadyProcessed()
                }
                is LedgerOutcome.Success -> {
                    // Transaction commit is the source of truth; immediate re-read can lag (eventual consistency).
                    Log.i(
                        TAG,
                        "reward_success uid=$uid key=$idempotencyKey xp+${outcome.xpAwarded} newXp=${outcome.newXp}",
                    )
                    GamificationRewardResult.Success(
                        xpAwarded = outcome.xpAwarded,
                        newXp = outcome.newXp,
                        newStreak = outcome.newStreak,
                        streakIncremented = outcome.streakIncremented,
                        todayStudyKey = outcome.todayStudyKey,
                    )
                }
            }
        } catch (e: Exception) {
            logRewardFailure(uid, idempotencyKey, action, "transaction", e)
            GamificationRewardResult.Failed(failureReason(e))
        }
    }

    private suspend fun ensureUserDocumentExists(uid: String, email: String) {
        val userRef = firestore.collection(UserFirestorePaths.USERS).document(uid)
        val snapshot = userRef.get().await()
        if (snapshot.exists()) return
        if (email.isBlank()) {
            error("user_document_missing_and_no_email")
        }
        userRef.set(
            UserFirestoreMapper.newUserFirestoreMap(uid, email),
            SetOptions.merge(),
        ).await()
    }

    private fun sanitizeLedgerKey(key: String): String =
        key.replace("/", "_").take(128)

    private fun failureReason(e: Exception): String = when (e) {
        is FirebaseFirestoreException -> "firestore_${e.code.name.lowercase()}"
        else -> e.javaClass.simpleName
    }

    private fun logRewardFailure(
        uid: String,
        idempotencyKey: String,
        action: XpRewardAction,
        stage: String,
        error: Exception?,
    ) {
        val code = (error as? FirebaseFirestoreException)?.code?.name
        Log.e(
            TAG,
            "reward_failed stage=$stage uid=$uid key=$idempotencyKey action=${action.name} code=$code",
            error,
        )
    }

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

    companion object {
        private const val TAG = "FirestoreGamification"
    }
}
