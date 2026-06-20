package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreGamificationRepository

object GamificationRepositoryProvider {


    val default: GamificationRepository = FirestoreGamificationRepository(
        userRepository = UserRepositoryProvider.default,
    )
}

object GamificationServiceProvider {
    val default: com.example.smartreview.data.gamification.GamificationService =
        com.example.smartreview.data.gamification.GamificationService()
}