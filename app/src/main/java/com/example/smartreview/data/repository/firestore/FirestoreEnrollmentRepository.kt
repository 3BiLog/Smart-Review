package com.example.smartreview.data.repository.firestore

import com.example.smartreview.data.model.CourseEnrollment
import com.example.smartreview.data.model.TransactionStatus
import com.example.smartreview.data.remote.firestore.EnrollmentFirestorePaths
import com.example.smartreview.data.remote.firestore.UserFirestorePaths
import com.example.smartreview.data.repository.EnrollmentRepository
import com.example.smartreview.data.repository.TransactionRepository
import com.example.smartreview.data.service.PaymentService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreEnrollmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : EnrollmentRepository {

    override suspend fun isEnrolled(userId: String, courseId: String): Boolean = withContext(Dispatchers.IO) {
        if (userId.isBlank() || courseId.isBlank()) return@withContext false
        val snap = enrollmentDoc(userId, courseId).get().await()
        snap.exists()
    }

    override fun observeEnrollment(userId: String, courseId: String): Flow<Boolean> = callbackFlow {
        if (userId.isBlank() || courseId.isBlank()) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }
        val registration = enrollmentDoc(userId, courseId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(false)
                return@addSnapshotListener
            }
            trySend(snapshot?.exists() == true)
        }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun getEnrolledCourseIds(userId: String): Set<String> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext emptySet()
        val snap = firestore
            .collection(UserFirestorePaths.USERS)
            .document(userId)
            .collection(EnrollmentFirestorePaths.ENROLLMENTS)
            .get()
            .await()
        snap.documents.mapNotNull { it.id.takeIf { id -> id.isNotBlank() } }.toSet()
    }

    private fun enrollmentDoc(userId: String, courseId: String) =
        firestore
            .collection(UserFirestorePaths.USERS)
            .document(userId)
            .collection(EnrollmentFirestorePaths.ENROLLMENTS)
            .document(courseId)
}

class RemoteTransactionRepository(
    private val paymentService: PaymentService = PaymentService(),
) : TransactionRepository {

    override suspend fun checkPaymentStatus(
        transactionId: String?,
        orderCode: Long?,
    ): TransactionStatus = withContext(Dispatchers.IO) {
        val result = paymentService.checkTransactionStatus(
            transactionId = transactionId,
            orderCode = orderCode,
        )
        result.fold(
            onSuccess = { TransactionStatus.fromString(it.status) },
            onFailure = { TransactionStatus.PENDING },
        )
    }
}
