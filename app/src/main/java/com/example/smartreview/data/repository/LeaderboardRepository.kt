package com.example.smartreview.data.repository

import com.example.smartreview.data.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {

    fun getBaseEntries(): List<LeaderboardEntry>

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>>
}
