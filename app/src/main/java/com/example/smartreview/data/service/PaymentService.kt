package com.example.smartreview.data.service

import com.example.smartreview.BuildConfig
import com.example.smartreview.data.model.CreateOrderRequest
import com.example.smartreview.data.model.CreateOrderResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class PaymentService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        private val API_BASE_URL: String = BuildConfig.PAYMENT_API_BASE_URL.trimEnd('/')
        private const val CREATE_ORDER_ENDPOINT = "/create-payos"
        private const val CHECK_TRANSACTION_ENDPOINT = "/check-transaction"
    }

    suspend fun createOrder(
        courseId: String,
        userId: String,
        userEmail: String,
        userName: String,
        amount: Long,
        courseName: String,
    ): Result<CreateOrderResponse> = withContext(Dispatchers.IO) {
        try {
            val request = CreateOrderRequest(
                courseId = courseId,
                userId = userId,
                userEmail = userEmail,
                userName = userName,
                amount = amount,
                courseName = courseName,
            )

            val json = gson.toJson(request)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = json.toRequestBody(mediaType)

            val httpRequest = Request.Builder()
                .url("$API_BASE_URL$CREATE_ORDER_ENDPOINT")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                android.util.Log.d("PaymentService", "Create Order Response: $responseBody")

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: $responseBody"),
                    )
                }

                val result = gson.fromJson(responseBody, CreateOrderResponse::class.java)
                if (result.success) {
                    Result.success(result)
                } else {
                    Result.failure(Exception(result.error ?: "Unknown error"))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PaymentService", "Error creating order", e)
            Result.failure(e)
        }
    }

    suspend fun checkTransactionStatus(
        transactionId: String? = null,
        orderCode: Long? = null,
    ): Result<PaymentStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val query = buildString {
                append("$API_BASE_URL$CHECK_TRANSACTION_ENDPOINT?")
                when {
                    !transactionId.isNullOrBlank() -> append("transactionId=$transactionId")
                    orderCode != null -> append("orderCode=$orderCode")
                    else -> error("transactionId or orderCode required")
                }
            }

            val request = Request.Builder()
                .url(query)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: $responseBody"),
                    )
                }

                val result = gson.fromJson(responseBody, PaymentStatusResponse::class.java)
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class PaymentStatusResponse(
    val status: String,
    val transactionId: String? = null,
    val orderCode: Long? = null,
    val courseId: String? = null,
    val paidAt: String? = null,
)
