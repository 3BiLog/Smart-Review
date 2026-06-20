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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

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

        val todayTimestamp = Timestamp(Date())
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

                val currentXP = profile?.xp ?: 0
                val currentStreak = profile?.streak ?: 0
                val lastStreakDate = profile?.lastStreakDate

                val streakUpdateResult = if (action.countsTowardStreak) {
                    computeStreakWithTimestamp(currentStreak, lastStreakDate, todayTimestamp)
                } else {
                    null
                }

                val userUpdates = mutableMapOf<String, Any>(
                    "totalXP" to FieldValue.increment(action.xpAmount.toLong()),
                    "xp" to FieldValue.increment(action.xpAmount.toLong()),
                )

                if (streakUpdateResult != null) {
                    userUpdates["currentStreak"] = streakUpdateResult.newStreak
                    userUpdates["streak"] = streakUpdateResult.newStreak
                    userUpdates["lastStreakDate"] = streakUpdateResult.todayTimestamp
                }

                userUpdates["updatedAt"] = FieldValue.serverTimestamp()

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
                    newXp = (currentXP + action.xpAmount).toInt(),
                    newStreak = streakUpdateResult?.newStreak ?: currentStreak,
                    streakIncremented = streakUpdateResult?.streakIncremented ?: false,
                    todayTimestamp = streakUpdateResult?.todayTimestamp ?: todayTimestamp,
                )
            }.await()

            if (outcome is LedgerOutcome.Success && outcome.xpAwarded > 0) {
                writeToXpLogs(uid, outcome.xpAwarded.toLong(), action.name, idempotencyKey)
            }

            when (outcome) {
                LedgerOutcome.AlreadyProcessed -> {
                    Log.i(TAG, "reward_already_processed uid=$uid key=$idempotencyKey")
                    GamificationRewardResult.AlreadyProcessed()
                }
                is LedgerOutcome.Success -> {
                    Log.i(
                        TAG,
                        "reward_success uid=$uid key=$idempotencyKey xp+${outcome.xpAwarded} newXp=${outcome.newXp}",
                    )
                    GamificationRewardResult.Success(
                        xpAwarded = outcome.xpAwarded,
                        newXp = outcome.newXp,
                        newStreak = outcome.newStreak.toInt(),
                        streakIncremented = outcome.streakIncremented,
                        todayStudyKey = "",  // Deprecated, use todayTimestamp
                    )
                }
            }
        } catch (e: Exception) {
            logRewardFailure(uid, idempotencyKey, action, "transaction", e)
            GamificationRewardResult.Failed(failureReason(e))
        }
    }

    private fun computeStreakWithTimestamp(
        currentStreak: Long,
        lastStreakDate: Timestamp?,
        today: Timestamp
    ): StreakUpdateResult? {
        val todayDate = today.toDate()
        val lastDate = lastStreakDate?.toDate()

        return when {
            lastDate == null -> StreakUpdateResult(
                newStreak = 1L,
                streakIncremented = true,
                todayTimestamp = today
            )
            isSameDay(lastDate, todayDate) -> StreakUpdateResult(
                newStreak = currentStreak,
                streakIncremented = false,
                todayTimestamp = today
            )
            isYesterday(lastDate, todayDate) -> StreakUpdateResult(
                newStreak = currentStreak + 1L,
                streakIncremented = true,
                todayTimestamp = today
            )
            else -> StreakUpdateResult(
                newStreak = 1L,
                streakIncremented = true,
                todayTimestamp = today
            )
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        cal2.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private suspend fun writeToXpLogs(uid: String, amount: Long, reason: String, activityType: String) {
        try {
            val xpLog = mapOf(
                "userId" to uid,
                "amount" to amount,
                "reason" to reason,
                "activityType" to activityType,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("xp_logs").add(xpLog).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write xp_log: ${e.message}")
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
            val newStreak: Long,
            val streakIncremented: Boolean,
            val todayTimestamp: Timestamp,
        ) : LedgerOutcome()
    }

    private data class StreakUpdateResult(
        val newStreak: Long,
        val streakIncremented: Boolean,
        val todayTimestamp: Timestamp
    )

    companion object {
        private const val TAG = "FirestoreGamification"
    }
}