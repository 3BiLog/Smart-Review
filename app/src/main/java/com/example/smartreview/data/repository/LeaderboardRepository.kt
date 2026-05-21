package com.example.smartreview.data.repository

import com.example.smartreview.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data access contract for the Leaderboard feature.
 * ViewModels depend on this interface — not on [com.example.smartreview.data.mock.MockLeaderboardData].
 */
interface LeaderboardRepository {

    /** One-shot leaderboard (base xp scores); tab scaling stays in the ViewModel. */
    fun getBaseEntries(): List<LeaderboardEntry>

    /** Realtime leaderboard ordered by xp descending; empty when guest. */
    fun observeLeaderboard(): Flow<List<LeaderboardEntry>>
}
