package com.example.smartreview.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.repository.AuthRepository
import com.example.smartreview.data.repository.AuthRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
) {
    val isEmailValid: Boolean
        get() = email.isNotBlank() && email.contains("@") && email.contains(".")
}

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepositoryProvider.default
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                error = null,
                successMessage = null
            )
        }
    }

    fun sendResetEmail(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        if (!_uiState.value.isEmailValid) {
            _uiState.update { it.copy(error = "Vui lòng nhập email hợp lệ") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

            val result = authRepository.sendPasswordResetEmail(email)

            if (result) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Link đặt lại mật khẩu đã được gửi đến $email. Vui lòng kiểm tra hộp thư."
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Không thể gửi email. Vui lòng kiểm tra lại email hoặc thử lại sau."
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { ForgotPasswordUiState() }
    }
}