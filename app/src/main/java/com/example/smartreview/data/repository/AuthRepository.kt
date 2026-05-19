package com.example.smartreview.data.repository

import com.example.smartreview.data.model.AuthResult
import com.example.smartreview.data.model.AuthUser

/**
 * Data access contract for authentication.
 * ViewModels depend on this interface — not on Firebase APIs directly.
 */
interface AuthRepository {

    suspend fun signInWithEmail(email: String, password: String): AuthResult

    suspend fun registerWithEmail(email: String, password: String): AuthResult

    fun signOut()

    fun getCurrentUser(): AuthUser?

    fun isAuthenticated(): Boolean
}
