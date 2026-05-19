package com.example.smartreview.data.repository

import com.example.smartreview.data.model.LeaderboardEntry

/**
 * Data access contract for the Leaderboard feature.
 * ViewModels depend on this interface — not on [com.example.smartreview.data.mock.MockLeaderboardData].
 */
interface LeaderboardRepository {

    /** Entries with base scores; tab scaling stays in the ViewModel. */
    fun getBaseEntries(): List<LeaderboardEntry>
}
