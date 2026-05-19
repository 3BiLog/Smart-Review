package com.example.smartreview.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentPinUiState(
    val pin:               String  = "",
    val pinLength:         Int     = 6,
    val courseName:        String  = "Advanced UI Systems",
    val orderId:           String  = "#SR-9920",
    val amount:            Long    = 450_000L,
    val isProcessing:      Boolean = false,
    val errorMessage:      String? = null,
    val remainingAttempts: Int     = 3,
    val isLocked:          Boolean = false,
    // Navigation triggers
    val navigateSuccess:   Boolean = false,
    val triggerShake:      Boolean = false,
    val showCancelDialog:  Boolean = false,
) {
    val isComplete: Boolean get() = pin.length == pinLength
    val isInputEnabled: Boolean get() = !isProcessing && !isLocked
}

class PaymentPinViewModel(
    private val courseId: String,
    private val amount:   Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentPinUiState(amount = amount))
    val uiState: StateFlow<PaymentPinUiState> = _uiState.asStateFlow()

    private val correctPin = "123456" // mock

    fun onDigit(digit: String) {
        val s = _uiState.value
        if (!s.isInputEnabled || s.pin.length >= s.pinLength) return
        _uiState.update { it.copy(pin = it.pin + digit, errorMessage = null) }
    }

    fun onDelete() {
        if (!_uiState.value.isInputEnabled) return
        _uiState.update { it.copy(pin = it.pin.dropLast(1), errorMessage = null) }
    }

    fun onSubmitPin() {
        val s = _uiState.value
        if (!s.isComplete || s.isProcessing || s.isLocked) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            delay(800) // simulate verification
            if (s.pin == correctPin) {
                _uiState.update { it.copy(isProcessing = false, navigateSuccess = true) }
            } else {
                val remaining = s.remainingAttempts - 1
                if (remaining <= 0) {
                    _uiState.update {
                        it.copy(
                            isProcessing      = false,
                            pin               = "",
                            isLocked          = true,
                            remainingAttempts = 0,
                            errorMessage      = "Tài khoản bị khoá tạm thời do nhập sai quá nhiều lần.",
                            triggerShake      = true,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessing      = false,
                            pin               = "",
                            remainingAttempts = remaining,
                            errorMessage      = "PIN không đúng. Còn $remaining lần thử.",
                            triggerShake      = true,
                        )
                    }
                }
            }
        }
    }

    fun onShakeConsumed()         = _uiState.update { it.copy(triggerShake = false) }
    fun onNavigated()             = _uiState.update { it.copy(navigateSuccess = false) }
    fun onShowCancelDialog()      = _uiState.update { it.copy(showCancelDialog = true) }
    fun onDismissCancelDialog()   = _uiState.update { it.copy(showCancelDialog = false) }

    companion object {
        fun factory(courseId: String, amount: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PaymentPinViewModel(courseId, amount) as T
            }
    }
}