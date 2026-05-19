package com.example.smartreview.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ─── Verify Phone (mock — unchanged) ───────────────────────────────────────────

data class VerifyPhoneUiState(
    val phoneNumber: String      = "283 835 2999",
    val otpDigits:   List<String> = List(4) { "" },
    val isLoading:   Boolean     = false,
    val error:       String?     = null,
) {
    val isComplete: Boolean get() = otpDigits.all { it.isNotEmpty() }
    val filledCount: Int get() = otpDigits.count { it.isNotEmpty() }
}

class VerifyPhoneViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VerifyPhoneUiState())
    val uiState: StateFlow<VerifyPhoneUiState> = _uiState.asStateFlow()

    fun pressDigit(digit: String) {
        val digits     = _uiState.value.otpDigits.toMutableList()
        val emptyIndex = digits.indexOfFirst { it.isEmpty() }
        if (emptyIndex != -1) {
            digits[emptyIndex] = digit
            _uiState.update { it.copy(otpDigits = digits, error = null) }
        }
    }

    fun pressBackspace() {
        val digits    = _uiState.value.otpDigits.toMutableList()
        val lastIndex = digits.indexOfLast { it.isNotEmpty() }
        if (lastIndex != -1) {
            digits[lastIndex] = ""
            _uiState.update { it.copy(otpDigits = digits) }
        }
    }

    fun onVerify(onSuccess: () -> Unit) {
        if (!_uiState.value.isComplete) {
            _uiState.update { it.copy(error = "Vui lòng nhập đủ 4 chữ số.") }
            return
        }
        onSuccess()
    }

    fun resendCode() { /* TODO: call API */ }
}
