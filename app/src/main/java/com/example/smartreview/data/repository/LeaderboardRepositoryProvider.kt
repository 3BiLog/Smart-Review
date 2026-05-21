package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreLeaderboardRepository
import com.example.smartreview.data.repository.mock.MockLeaderboardRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 *
 * - [default] uses Firestore when authenticated; mock fallback only on query errors.
 *   Guests receive empty leaderboard (no silent mock masking).
 * - [mock] always uses local mock data (debug/tests).
 */
object LeaderboardRepositoryProvider {

    val mock: LeaderboardRepository = MockLeaderboardRepository()

    val default: LeaderboardRepository = FirestoreLeaderboardRepository(fallback = mock)
}
