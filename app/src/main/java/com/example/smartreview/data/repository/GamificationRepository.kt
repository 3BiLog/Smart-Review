package com.example.smartreview.data.repository

import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.gamification.XpRewardAction

/**
 * Atomic XP/streak rewards for the signed-in user (Firestore transaction + idempotency ledger).
 */
interface GamificationRepository {

    /**
     * Applies [action] for [uid] once per [idempotencyKey].
     * Caller must ensure [uid] matches the authenticated Firebase user.
     */
    suspend fun applyReward(
        uid: String,
        action: XpRewardAction,
        idempotencyKey: String,
    ): GamificationRewardResult
}
