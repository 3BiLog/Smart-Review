package com.example.smartreview.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartreview.data.auth.AuthSession
import com.example.smartreview.data.model.TransactionStatus
import com.example.smartreview.data.repository.CourseRepository
import com.example.smartreview.data.repository.CourseRepositoryProvider
import com.example.smartreview.data.repository.EnrollmentRepository
import com.example.smartreview.data.repository.EnrollmentRepositoryProvider
import com.example.smartreview.data.repository.TransactionRepository
import com.example.smartreview.data.repository.TransactionRepositoryProvider
import com.example.smartreview.data.service.PaymentService
import com.example.smartreview.data.service.PaymentStatusResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentMethodUiState(
    val paymentMethods: List<PaymentMethodOption> = emptyList(),
    val selectedMethodId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val coursePrice: Long = 0,
    val subtotal: Long = 0L,
    val transactionFee: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val checkoutUrl: String? = null,
    val isProcessingPayment: Boolean = false,
    val pendingTransactionId: String? = null,
    val pendingOrderCode: Long? = null,
    val paymentSuccess: Boolean = false,
) {
    val total: Long get() = subtotal + transactionFee
    val selectedMethod: PaymentMethodOption?
        get() = paymentMethods.find { it.id == selectedMethodId }
}

class PaymentMethodViewModel(
    private val courseId: String,
    private val courseName: String = "",
    private val coursePrice: Long = 0,
    private val courseRepository: CourseRepository = CourseRepositoryProvider.default,
    private val transactionRepository: TransactionRepository = TransactionRepositoryProvider.default,
    private val enrollmentRepository: EnrollmentRepository = EnrollmentRepositoryProvider.default,
    private val paymentService: PaymentService = PaymentService(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PaymentMethodUiState(
            courseId = courseId,
            courseName = courseName,
            coursePrice = coursePrice,
            subtotal = coursePrice,
        )
    )
    val uiState: StateFlow<PaymentMethodUiState> = _uiState.asStateFlow()

    init {
        loadPaymentMethods()
        loadCourseInfo()
    }

    fun selectMethod(id: String) =
        _uiState.update { it.copy(selectedMethodId = id, errorMessage = null) }

    fun confirmPayment() {
        if (_uiState.value.selectedMethodId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn phương thức thanh toán.") }
            return
        }
        createPayOSOrder()
    }

    fun onReturnedFromCheckout() {
        val state = _uiState.value
        if (state.pendingTransactionId == null && state.pendingOrderCode == null) return
        checkPaymentStatus()
    }

    fun consumeCheckoutUrl() {
        _uiState.update { it.copy(checkoutUrl = null) }
    }

    fun consumePaymentSuccess() {
        _uiState.update { it.copy(paymentSuccess = false) }
    }

    private fun loadCourseInfo() {
        if (courseName.isNotBlank() && coursePrice > 0) return
        viewModelScope.launch {
            val course = courseRepository.getCourseById(courseId) ?: return@launch
            _uiState.update {
                it.copy(
                    courseName = course.title,
                    coursePrice = course.price,
                    subtotal = course.price,
                )
            }
        }
    }

    private fun createPayOSOrder() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            _uiState.update {
                it.copy(errorMessage = "Vui lòng đăng nhập để thanh toán.")
            }
            return
        }

        val state = _uiState.value
        if (state.coursePrice <= 0) {
            _uiState.update { it.copy(errorMessage = "Khóa học này miễn phí.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isProcessingPayment = true, errorMessage = null)
            }

            val result = paymentService.createOrder(
                courseId = courseId,
                userId = user.uid,
                userEmail = user.email ?: "",
                userName = user.displayName ?: "User",
                amount = state.coursePrice,
                courseName = state.courseName,
            )

            result.fold(
                onSuccess = { response ->
                    android.util.Log.d("PaymentVM", "Order created: ${response.transactionId}")
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            checkoutUrl = response.checkoutUrl,
                            pendingTransactionId = response.transactionId,
                            pendingOrderCode = response.transactionId.toLongOrNull(),
                        )
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("PaymentVM", "Create order error", error)
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = error.message ?: "Không thể tạo đơn hàng",
                        )
                    }
                }
            )
        }
    }

    private fun checkPaymentStatus() {
        val state = _uiState.value
        android.util.Log.d(
            "PaymentVM",
            "Checking status: transactionId=${state.pendingTransactionId}, orderCode=${state.pendingOrderCode}"
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true, errorMessage = null) }

            var result: Result<PaymentStatusResponse>? = null
            var retryCount = 0
            val maxRetries = 4
            var delayMs = 1000L

            while (retryCount < maxRetries && result == null) {
                val currentResult = paymentService.checkTransactionStatus(
                    transactionId = state.pendingTransactionId,
                    orderCode = state.pendingOrderCode,
                )
                if (currentResult.isSuccess && currentResult.getOrNull()?.status == "pending") {
                    if (retryCount < maxRetries - 1) {
                        android.util.Log.d("PaymentVM", "Status pending, retry in ${delayMs}ms (attempt ${retryCount+1}/$maxRetries)")
                        delay(delayMs)
                        delayMs *= 2 // exponential backoff
                    } else {
                        result = currentResult
                    }
                } else {
                    result = currentResult
                }
                retryCount++
            }

            if (result == null) {
                _uiState.update {
                    it.copy(
                        isProcessingPayment = false,
                        errorMessage = "Không thể xác nhận trạng thái thanh toán. Vui lòng kiểm tra lại sau."
                    )
                }
                return@launch
            }

            result.fold(
                onSuccess = { response ->
                    android.util.Log.d(
                        "PaymentVM",
                        "Final Status response: ${response.status}, paidAt=${response.paidAt}"
                    )
                    val status = TransactionStatus.fromString(response.status)
                    when (status) {
                        TransactionStatus.SUCCESS -> {
                            val uid = AuthSession.state.value.uid
                            if (!uid.isNullOrBlank()) {
                                enrollmentRepository.isEnrolled(uid, courseId)
                            }
                            _uiState.update {
                                it.copy(
                                    isProcessingPayment = false,
                                    paymentSuccess = true,
                                    pendingTransactionId = null,
                                    pendingOrderCode = null,
                                )
                            }
                        }
                        TransactionStatus.FAILED, TransactionStatus.CANCELLED -> {
                            _uiState.update {
                                it.copy(
                                    isProcessingPayment = false,
                                    errorMessage = "Thanh toán thất bại. Vui lòng thử lại."
                                )
                            }
                        }
                        TransactionStatus.PENDING -> {
                            _uiState.update {
                                it.copy(
                                    isProcessingPayment = false,
                                    errorMessage = "Đang chờ xác nhận. Vui lòng thử lại sau."
                                )
                            }
                        }
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("PaymentVM", "Check status error", error)
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = "Lỗi kiểm tra trạng thái: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun loadPaymentMethods() {
        val methods = listOf(
            PaymentMethodOption(
                id = "payos",
                type = PaymentType.E_WALLET,
                title = "PayOS - QR Code",
                subtitle = "Thanh toán qua QR Code, chuyển khoản ngân hàng",
                isDefault = true,
            )
        )
        _uiState.update {
            it.copy(
                paymentMethods = methods,
                selectedMethodId = "payos",
            )
        }
    }

    companion object {
        fun factory(
            courseId: String,
            courseName: String = "",
            coursePrice: Long = 0,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    PaymentMethodViewModel(courseId, courseName, coursePrice) as T
            }
    }
}