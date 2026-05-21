package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.mock.MockLeaderboardData
import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.repository.LeaderboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mock implementation backed by [MockLeaderboardData].
 * Replace with remote/local sources when Retrofit/Room are introduced.
 */
class MockLeaderboardRepository : LeaderboardRepository {

    override fun getBaseEntries(): List<LeaderboardEntry> = MockLeaderboardData.baseEntries

    override fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = flowOf(getBaseEntries())
}
