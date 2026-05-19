package com.example.smartreview.data.repository.mock

import com.example.smartreview.data.mock.MockLeaderboardData
import com.example.smartreview.data.model.LeaderboardEntry
import com.example.smartreview.data.repository.LeaderboardRepository

/**
 * Mock implementation backed by [MockLeaderboardData].
 * Replace with remote/local sources when Retrofit/Room are introduced.
 */
class MockLeaderboardRepository : LeaderboardRepository {

    override fun getBaseEntries(): List<LeaderboardEntry> = MockLeaderboardData.baseEntries
}
