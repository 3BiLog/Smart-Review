package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreLeaderboardRepository
import com.example.smartreview.data.repository.mock.MockLeaderboardRepository

object LeaderboardRepositoryProvider {

    val mock: LeaderboardRepository = MockLeaderboardRepository()

    val default: LeaderboardRepository = FirestoreLeaderboardRepository(fallback = mock)
}
