package com.example.smartreview.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthSessionState(
    val isAuthenticated: Boolean = false,
    val uid: String? = null,
    val email: String? = null,
)

/**
 * Single source of truth for Firebase Auth session across ViewModels.
 * Keeps UI state in sync when sign-in/sign-out happens outside [AuthViewModel].
 */
object AuthSession {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(readCurrentState())
    val state: StateFlow<AuthSessionState> = _state.asStateFlow()

    private var started = false

    private val authStateListener = FirebaseAuth.AuthStateListener {
        _state.value = readCurrentState()
    }

    fun ensureStarted() {
        if (started) return
        started = true
        firebaseAuth.addAuthStateListener(authStateListener)
        _state.value = readCurrentState()
    }

    fun refresh() {
        _state.value = readCurrentState()
    }

    private fun readCurrentState(): AuthSessionState {
        val user = firebaseAuth.currentUser
        return AuthSessionState(
            isAuthenticated = user != null,
            uid = user?.uid,
            email = user?.email,
        )
    }
}
