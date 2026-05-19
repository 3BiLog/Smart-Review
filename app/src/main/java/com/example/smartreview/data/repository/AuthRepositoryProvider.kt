package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firebase.FirebaseAuthRepository

/**
 * Lightweight access point until DI (e.g. Hilt) is added.
 * Swap [default] in tests or when wiring a mock implementation.
 */
object AuthRepositoryProvider {
    val default: AuthRepository = FirebaseAuthRepository()
}
