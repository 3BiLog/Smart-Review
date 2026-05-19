package com.example.smartreview.ui.screens.payment

import androidx.compose.ui.graphics.Color
import com.example.smartreview.ui.theme.*

// ─── Routes ──────────────────────────────────────────────────────────────────
object PaymentRoutes {
    const val METHOD  = "payment_method/{courseId}"
    const val PIN     = "payment_pin/{courseId}/{amount}"
    const val SUCCESS = "purchase_success/{courseId}"

    fun methodRoute(courseId: String)               = "payment_method/$courseId"
    fun pinRoute(courseId: String, amount: Long)    = "payment_pin/$courseId/$amount"
    fun successRoute(courseId: String)              = "purchase_success/$courseId"
}

// ─── Domain models ────────────────────────────────────────────────────────────
enum class PaymentType { CREDIT_CARD, DEBIT_CARD, E_WALLET }

data class PaymentMethodOption(
    val id:         String,
    val type:       PaymentType,
    val title:      String,
    val subtitle:   String,
    val cardHolder: String   = "",
    val cardNumber: String   = "",   // last 4 digits
    val expiry:     String   = "",
    val balance:    Long?    = null, // VND, null if not a wallet
    val isDefault:  Boolean  = false,
)

data class ReceiptInfo(
    val orderId:    String,
    val date:       String,
    val amount:     Long,
    val methodName: String,
)