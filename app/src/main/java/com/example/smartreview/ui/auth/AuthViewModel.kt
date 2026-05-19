package com.example.smartreview.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.model.AuthResult
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUserEmail: String? = null,
    val currentUserId: String? = null,
) {
    val isLoginFormValid: Boolean get() = email.isNotBlank() && password.length >= 6
    val isSignUpFormValid: Boolean get() = isLoginFormValid && termsAccepted
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun togglePasswordVisibility() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onTermsToggle() = _uiState.update { it.copy(termsAccepted = !it.termsAccepted) }

    fun checkAuthState() {
        val user = authRepository.getCurrentUser()
        _uiState.update {
            it.copy(
                isAuthenticated = authRepository.isAuthenticated(),
                currentUserEmail = user?.email,
                currentUserId = user?.uid,
            )
        }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isLoginFormValid) {
            _uiState.update { it.copy(error = "Vui lòng điền đầy đủ thông tin hợp lệ.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithEmail(state.email, state.password)) {
                is AuthResult.Success -> {
                    applyAuthenticatedUser(result.user.email, result.user.uid)
                    onSuccess()
                }
                is AuthResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isSignUpFormValid) {
            _uiState.update { it.copy(error = "Vui lòng điền đầy đủ và đồng ý điều khoản.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.registerWithEmail(state.email, state.password)) {
                is AuthResult.Success -> {
                    applyAuthenticatedUser(result.user.email, result.user.uid)
                    onSuccess()
                }
                is AuthResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentUserEmail = null,
                currentUserId = null,
                isLoading = false,
                error = null,
            )
        }
    }

    private fun applyAuthenticatedUser(email: String, uid: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                isAuthenticated = true,
                currentUserEmail = email,
                currentUserId = uid,
            )
        }
    }
}
