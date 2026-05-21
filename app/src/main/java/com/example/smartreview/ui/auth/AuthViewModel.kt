package com.example.smartreview.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.AuthResult
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import com.example.smartreview.data.repository.UserRepository
import com.example.smartreview.data.repository.UserRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val fullName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUserEmail: String? = null,
    val currentUserId: String? = null,
) {
    val isLoginFormValid: Boolean
        get() = email.isNotBlank() && password.length >= MIN_PASSWORD_LENGTH

    val passwordsMatch: Boolean
        get() = confirmPassword.isEmpty() || confirmPassword == password

    val isSignUpFormValid: Boolean
        get() = fullName.trim().length >= MIN_NAME_LENGTH &&
            email.isNotBlank() &&
            password.length >= MIN_PASSWORD_LENGTH &&
            confirmPassword == password &&
            termsAccepted

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
        const val MIN_NAME_LENGTH = 2
    }
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.default,
    private val userRepository: UserRepository = UserRepositoryProvider.default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        AuthSession.ensureStarted()
        syncFromSession()
        viewModelScope.launch {
            AuthSession.state.collect { syncFromSession() }
        }
    }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }

    fun onFullNameChange(value: String) = _uiState.update { it.copy(fullName = value, error = null) }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, error = null) }

    fun togglePasswordVisibility() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun toggleConfirmPasswordVisibility() =
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

    fun onTermsToggle() = _uiState.update { it.copy(termsAccepted = !it.termsAccepted) }

    fun checkAuthState() = syncFromSession()

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.isLoginFormValid) {
            _uiState.update { it.copy(error = "Vui lòng nhập email và mật khẩu (tối thiểu 6 ký tự).") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signInWithEmail(state.email, state.password)) {
                is AuthResult.Success -> {
                    ensureFirestoreProfile(result.user.uid, result.user.email, displayName = null)
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
        val validationError = signUpValidationError()
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.registerWithEmail(state.email, state.password)) {
                is AuthResult.Success -> {
                    ensureFirestoreProfile(
                        uid = result.user.uid,
                        email = result.user.email,
                        displayName = state.fullName.trim(),
                    )
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
        AuthSession.refresh()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                currentUserEmail = null,
                currentUserId = null,
                isLoading = false,
                error = null,
                fullName = "",
                confirmPassword = "",
            )
        }
    }

    fun showSocialLoginUnavailable() {
        _uiState.update {
            it.copy(error = "Đăng nhập Google/Apple chưa được kích hoạt. Vui lòng dùng email.")
        }
    }

    fun clearSignUpFields() {
        _uiState.update {
            it.copy(
                fullName = "",
                confirmPassword = "",
                termsAccepted = false,
                error = null,
            )
        }
    }

    private fun signUpValidationError(): String? {
        val state = _uiState.value
        return when {
            state.fullName.trim().length < AuthUiState.MIN_NAME_LENGTH ->
                "Họ tên phải có ít nhất ${AuthUiState.MIN_NAME_LENGTH} ký tự."
            state.email.isBlank() || !state.email.contains("@") ->
                "Vui lòng nhập email hợp lệ."
            state.password.length < AuthUiState.MIN_PASSWORD_LENGTH ->
                "Mật khẩu tối thiểu ${AuthUiState.MIN_PASSWORD_LENGTH} ký tự."
            state.confirmPassword != state.password ->
                "Mật khẩu xác nhận không khớp."
            !state.termsAccepted ->
                "Vui lòng đồng ý điều khoản sử dụng."
            else -> null
        }
    }

    private fun applyAuthenticatedUser(email: String, uid: String) {
        AuthSession.refresh()
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

    private suspend fun ensureFirestoreProfile(
        uid: String,
        email: String,
        displayName: String?,
    ) {
        userRepository.ensureUserProfileExists(uid, email, displayName)
    }

    private fun syncFromSession() {
        val session = AuthSession.state.value
        _uiState.update {
            it.copy(
                isAuthenticated = session.isAuthenticated,
                currentUserEmail = session.email,
                currentUserId = session.uid,
            )
        }
    }
}
