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

data class PaymentMethodUiState(
    val paymentMethods:   List<PaymentMethodOption> = emptyList(),
    val selectedMethodId: String                    = "",
    val subtotal:         Long                      = 450_000L,
    val transactionFee:   Long                      = 0L,
    val isLoading:        Boolean                   = false,
    val errorMessage:     String?                   = null,
    // Navigation trigger
    val navigateToPin:    Boolean                   = false,
) {
    val total: Long get() = subtotal + transactionFee
    val selectedMethod: PaymentMethodOption?
        get() = paymentMethods.find { it.id == selectedMethodId }
}

class PaymentMethodViewModel(private val courseId: String) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodUiState())
    val uiState: StateFlow<PaymentMethodUiState> = _uiState.asStateFlow()

    init { loadMockData() }

    fun selectMethod(id: String) =
        _uiState.update { it.copy(selectedMethodId = id, errorMessage = null) }

    fun confirmPayment() {
        if (_uiState.value.selectedMethodId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn phương thức thanh toán.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(900) // simulate network
            _uiState.update { it.copy(isLoading = false, navigateToPin = true) }
        }
    }

    fun onNavigated() = _uiState.update { it.copy(navigateToPin = false) }

    private fun loadMockData() {
        val methods = listOf(
            PaymentMethodOption(
                id = "card_visa",
                type       = PaymentType.CREDIT_CARD,
                title      = "Visa Ending in 8842",
                subtitle   = "Hết hạn 12/2026",
                cardHolder = "ALEX NGUYEN",
                cardNumber = "8842",
                expiry     = "12/26",
                isDefault  = true,
            ),
            PaymentMethodOption(
                id      = "wallet_smart",
                type    = PaymentType.E_WALLET,
                title   = "Ví Smart Pay",
                subtitle = "Số dư: 1,250,000đ",
                balance = 1_250_000L,
            ),
        )
        _uiState.update { it.copy(paymentMethods = methods, selectedMethodId = "card_visa") }
    }

    companion object {
        fun factory(courseId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PaymentMethodViewModel(courseId) as T
            }
    }
}