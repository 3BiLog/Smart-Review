package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firebase.FirebaseAuthRepository

object AuthRepositoryProvider {
    val default: AuthRepository = FirebaseAuthRepository()
}
