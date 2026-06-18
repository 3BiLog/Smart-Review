package com.example.smartreview.data.repository

import com.example.smartreview.data.repository.firestore.FirestoreEnrollmentRepository
import com.example.smartreview.data.repository.firestore.RemoteTransactionRepository

object EnrollmentRepositoryProvider {
    val default: EnrollmentRepository = FirestoreEnrollmentRepository()
}

object TransactionRepositoryProvider {
    val default: TransactionRepository = RemoteTransactionRepository()
}
