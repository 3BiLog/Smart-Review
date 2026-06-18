package com.example.smartreview.data.repository

import com.example.smartreview.data.model.CourseEnrollment
import com.example.smartreview.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

interface EnrollmentRepository {
    suspend fun isEnrolled(userId: String, courseId: String): Boolean
    fun observeEnrollment(userId: String, courseId: String): Flow<Boolean>
    suspend fun getEnrolledCourseIds(userId: String): Set<String>
}

interface TransactionRepository {
    suspend fun checkPaymentStatus(transactionId: String? = null, orderCode: Long? = null): TransactionStatus
}
