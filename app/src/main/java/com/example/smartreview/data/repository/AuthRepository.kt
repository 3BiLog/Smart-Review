package com.example.smartreview.data.repository

import com.example.smartreview.data.model.AuthResult
import com.example.smartreview.data.model.AuthUser

interface AuthRepository {

    suspend fun signInWithEmail(email: String, password: String): AuthResult

    suspend fun registerWithEmail(email: String, password: String): AuthResult

    suspend fun sendPasswordResetEmail(email: String): Boolean

    suspend fun updatePassword(currentPassword: String, newPassword: String): AuthResult

    fun signOut()

    fun getCurrentUser(): AuthUser?

    fun isAuthenticated(): Boolean
}
