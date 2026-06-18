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
import com.google.firebase.auth.FirebaseAuth
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
        ),
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
                    if (response.success && response.data != null) {
                        _uiState.update {
                            it.copy(
                                isProcessingPayment = false,
                                checkoutUrl = response.data.checkoutUrl,
                                pendingTransactionId = response.data.transactionId,
                                pendingOrderCode = response.data.orderCode,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isProcessingPayment = false,
                                errorMessage = response.error ?: "Không thể tạo đơn hàng",
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = error.message ?: "Lỗi kết nối đến server",
                        )
                    }
                },
            )
        }
    }

    private fun checkPaymentStatus() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true, errorMessage = null) }

            val status = transactionRepository.checkPaymentStatus(
                transactionId = state.pendingTransactionId,
                orderCode = state.pendingOrderCode,
            )

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
                            errorMessage = "Thanh toán chưa thành công. Vui lòng thử lại.",
                        )
                    }
                }
                TransactionStatus.PENDING -> {
                    _uiState.update {
                        it.copy(
                            isProcessingPayment = false,
                            errorMessage = "Đang chờ xác nhận thanh toán. Vui lòng thử lại sau vài giây.",
                        )
                    }
                }
            }
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
            ),
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
