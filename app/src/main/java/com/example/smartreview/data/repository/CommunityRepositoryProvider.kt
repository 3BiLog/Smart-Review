package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreCommunityRepository
import com.example.smartreview.data.repository.mock.MockCommunityRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 *
 * - [default] tries Firestore first, falls back to [mock] on error/empty/offline.
 * - [mock] always uses local mock data (debug/tests).
 * - [realtime] exposes Firestore snapshot listeners for a future ViewModel phase.
 */
object CommunityRepositoryProvider {

    val mock: CommunityRepository = MockCommunityRepository()

    private val firestoreRepository = FirestoreCommunityRepository(fallback = mock)

    val default: CommunityRepository = firestoreRepository

    val realtime: CommunityRealtimeRepository = firestoreRepository
}
