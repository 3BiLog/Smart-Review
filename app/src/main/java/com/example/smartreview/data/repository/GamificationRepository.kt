package com.example.smartreview.data.repository

import com.example.smartreview.data.gamification.GamificationRewardResult
import com.example.smartreview.data.gamification.XpRewardAction

interface GamificationRepository {

    suspend fun applyReward(
        uid: String,
        action: XpRewardAction,
        idempotencyKey: String,
    ): GamificationRewardResult
}
