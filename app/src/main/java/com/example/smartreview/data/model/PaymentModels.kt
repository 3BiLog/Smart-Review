package com.example.smartreview.data.model

data class CreateOrderRequest(
    val courseId: String,
    val userId: String,
    val userEmail: String,
    val userName: String,
    val amount: Long,
    val courseName: String,
)

data class PaymentOrderResponse(
    val checkoutUrl: String,
    val qrCode: String? = null,
    val transactionId: String,
)

data class Transaction(
    val id: String,
    val orderCode: Long,
    val courseId: String,
    val userId: String,
    val amount: Long,
    val status: TransactionStatus,
    val courseName: String = "",
    val checkoutUrl: String? = null,
)

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED,
    ;

    companion object {
        fun fromString(value: String?): TransactionStatus = when (value?.lowercase()) {
            "success", "paid" -> SUCCESS
            "failed", "cancelled", "canceled" -> FAILED
            else -> PENDING
        }
    }
}

data class CourseEnrollment(
    val courseId: String,
    val userId: String,
    val orderCode: Long = 0,
    val amount: Long = 0,
    val transactionId: String = "",
)