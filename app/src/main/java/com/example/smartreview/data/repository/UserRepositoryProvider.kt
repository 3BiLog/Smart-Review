package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreUserRepository
import com.example.smartreview.data.repository.mock.MockUserRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 *
 * - [default] reads Firestore users/{uid}, falls back to [mock] on error.
 * - [mock] always uses local mock data (debug/tests).
 */
object UserRepositoryProvider {

    val mock: UserRepository = MockUserRepository()

    val default: UserRepository = FirestoreUserRepository(fallback = mock)
}
