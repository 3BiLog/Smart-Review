package com.example.smartreview.ui.screens.payment

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.smartreview.ui.theme.*

object PaymentRoutes {
    const val METHOD  = "payment_method/{courseId}/{courseName}/{coursePrice}"
    const val PIN     = "payment_pin/{courseId}/{amount}"
    const val SUCCESS = "purchase_success/{courseId}/{justPaid}"  // thêm justPaid

    fun successRoute(courseId: String, justPaid: Boolean = false) =
        "purchase_success/$courseId/$justPaid"

    fun methodRoute(courseId: String, courseName: String = "", coursePrice: Long = 0): String {
        val encodedName = Uri.encode(courseName.ifBlank { "course" })
        return "payment_method/$courseId/$encodedName/$coursePrice"
    }

    fun pinRoute(courseId: String, amount: Long) = "payment_pin/$courseId/$amount"
    // Xóa overload cũ để tránh nhầm lẫn
}

enum class PaymentType { CREDIT_CARD, DEBIT_CARD, E_WALLET }

data class PaymentMethodOption(
    val id:         String,
    val type:       PaymentType,
    val title:      String,
    val subtitle:   String,
    val cardHolder: String   = "",
    val cardNumber: String   = "",
    val expiry:     String   = "",
    val balance:    Long?    = null,
    val isDefault:  Boolean  = false,
)

data class ReceiptInfo(
    val orderId:    String,
    val date:       String,
    val amount:     Long,
    val methodName: String,
)