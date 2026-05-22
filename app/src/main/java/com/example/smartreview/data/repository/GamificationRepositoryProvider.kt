package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreGamificationRepository
import com.example.smartreview.data.repository.mock.MockGamificationRepository
import com.example.smartreview.data.repository.mock.MockUserRepository

object GamificationRepositoryProvider {

    private val mockUserRepository: MockUserRepository =
        UserRepositoryProvider.mock as MockUserRepository

    val mock: GamificationRepository = MockGamificationRepository(mockUserRepository)

    val default: GamificationRepository = FirestoreGamificationRepository(
        userRepository = UserRepositoryProvider.default,
    )
}

object GamificationServiceProvider {
    val default: com.example.smartreview.data.gamification.GamificationService =
        com.example.smartreview.data.gamification.GamificationService()
}
